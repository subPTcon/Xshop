package org.michael.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.payment.client.OrderClient;
import org.michael.payment.client.dto.OrderDTO;
import org.michael.payment.dto.PaymentCallbackDTO;
import org.michael.payment.dto.PaymentCreateDTO;
import org.michael.payment.enums.PaymentStatus;
import org.michael.payment.event.PaymentEventProducer;
import org.michael.payment.event.PaymentSuccessEvent;
import org.michael.payment.mapper.PaymentMapper;
import org.michael.payment.pojo.Payment;
import org.michael.payment.service.PaymentService;
import org.michael.payment.vo.PaymentCreateVO;
import org.michael.payment.vo.PaymentDetailVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;

    private final OrderClient orderClient;

    private static final String MOCK_SIGN = "xshop-mock-sign";

    private final PaymentEventProducer paymentEventProducer;

    @Override
    public PaymentCreateVO createPayment(Long userId, PaymentCreateDTO dto) {
        // 1.查询订单
        OrderDTO order = getOrder(dto.getOrderNo());

        // 2.校验订单属于当前用户
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 3.只有待支付订单才能发起支付
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        // 4.同一个订单如果已经有支付单，直接返回原支付单
        Payment exisitingPayment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(
                                Payment::getOrderNo,
                                dto.getOrderNo()
                        )
        );
        if (exisitingPayment != null) {
            return buildPaymentCreateVO(exisitingPayment);
        }

        // 5.生成支付单号
        String paymentNo = IdWorker.getIdStr();
        Payment payment = new Payment();
        payment.setPaymentNo(paymentNo);
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(order.getUserId());
        payment.setAmount(order.getTotalAmount());
        payment.setPayChannel(dto.getPayChannel());
        payment.setStatus(0);

        try {
            paymentMapper.insert(payment);
        } catch (DuplicateKeyException e) {
            /**
             * 并发情况下可能两个请求同时：
             *
             * 查询 -> 都发现不存在
             * INSERT -> 一个成功，一个唯一键冲突
             *
             * 冲突后重新查已有支付订单即可
             */
            Payment existed = paymentMapper.selectOne(
                    new LambdaQueryWrapper<Payment>()
                            .eq(
                                    Payment::getOrderNo,
                                    dto.getOrderNo()
                            )
            );
            if (existed != null) {
                return buildPaymentCreateVO(existed);
            }

            throw new BusinessException(ErrorCode.PAYMENT_CREATE_CONFLICT);
        }

        log.info("创建支付单成功, paymentNo={}, orderNo={}, amount={}", paymentNo, order.getOrderNo(), order.getTotalAmount());

        return buildPaymentCreateVO(payment);
    }

    @Override
    public Boolean handleCallback(PaymentCallbackDTO dto) {
        // 1.验签
        checkSign(dto);

        // 2.查询支付单
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(
                                Payment::getPaymentNo,
                                dto.getPaymentNo()
                        )
        );
        if (payment == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        /**
         * 3.校验金额
         */
        if (payment.getAmount().compareTo(dto.getAmount()) != 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_INVALID);
        }

        /**
         * 4.处理支付失败
         */
        if (PaymentStatus.FAILED.getCode().equals(dto.getStatus())) {
            handleFailed(payment);
            return Boolean.TRUE;
        }

        /**
         * 目前只接受SUCCESS/FAILED
         */
        if (!PaymentStatus.SUCCESS.getCode().equals(dto.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }

        LocalDateTime payTime = dto.getPayTime() == null ? LocalDateTime.now() : dto.getPayTime();

        /**
         * 5.更新状态 PENDING -> SUCCESS
         */
        if (PaymentStatus.PENDING.getCode().equals(payment.getStatus())) {
            int affected = paymentMapper.update(
                    null,
                    new LambdaUpdateWrapper<Payment>()
                            .eq(
                                    Payment::getPaymentNo,
                                    payment.getPaymentNo()
                            )
                            .eq(
                                    Payment::getStatus,
                                    PaymentStatus.PENDING.getCode()
                            )
                            .set(
                                    Payment::getStatus,
                                    PaymentStatus.SUCCESS.getCode()
                            )
                            .set(
                                    Payment::getPayTime,
                                    payTime
                            )
            );

            /**
             * affected = 0
             * 很可能是另一个重复回调刚刚处理成功
             */
            if (affected == 0) {
                Payment latest = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>()
                                .eq(
                                        Payment::getPaymentNo,
                                        payment.getPaymentNo()
                                )
                );
                if (latest == null || !PaymentStatus.SUCCESS.getCode().equals(latest.getStatus())) {
                    throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID);
                }
            }
        } else if (!PaymentStatus.SUCCESS.getCode().equals(payment.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID);
        }

        /**
         * 6.即使payment本来已经SUCCESS
         * 这里仍然重新通知order-service
         */
        notifyOrderPaymentSuccess(payment.getOrderNo());

        PaymentSuccessEvent event = new PaymentSuccessEvent(
                UUID.randomUUID().toString(),
                payment.getPaymentNo(),
                payment.getOrderNo(),
                payment.getUserId(),
                payment.getAmount()
        );
        paymentEventProducer.sendPaymentSuccess(event);

        return Boolean.TRUE;
    }

    private void notifyOrderPaymentSuccess(String orderNo) {
        try {
            Result<Boolean> result = orderClient.paymentSuccess(orderNo);
            if (result == null || result.getData() == null || !Boolean.TRUE.equals(result.getData())) {
                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("通知 order-service 支付成功失败，orderNo={}", orderNo, e);
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }
    }

    private void handleFailed(Payment payment) {
        /**
         * 已经 SUCCESS 不能再被失败回调覆盖
         */
        if (PaymentStatus.SUCCESS.getCode().equals(payment.getStatus())) {
            return;
        }

        if (!PaymentStatus.PENDING.getCode().equals(payment.getStatus())) {
            return;
        }

        paymentMapper.update(
                null,
                new LambdaUpdateWrapper<Payment>()
                        .eq(
                                Payment::getPaymentNo,
                                payment.getPaymentNo()
                        )
                        .eq(
                                Payment::getStatus,
                                PaymentStatus.PENDING.getCode()
                        )
                        .set(
                                Payment::getStatus,
                                PaymentStatus.FAILED.getCode()
                        )
        );
    }

    @Override
    public PaymentDetailVO getPayment(Long userId, String paymentNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(
                                Payment::getPaymentNo,
                                paymentNo
                        )
                        .eq(
                                Payment::getUserId,
                                userId
                        )
        );
        if (payment == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        PaymentDetailVO vo = new PaymentDetailVO();
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setAmount(payment.getAmount());
        vo.setStatus(payment.getStatus());
        vo.setPayChannel(payment.getPayChannel());
        return vo;
    }

    private void notifyOrderPaid(String orderNo, LocalDateTime payTime) {
        try {
            Result<Boolean> result = orderClient.paymentSuccess(orderNo);
            if (result == null || result.getData() == null || !Boolean.TRUE.equals(result.getData())) {
                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("通知order-service支付成功失败，orderNo={}", orderNo, e);
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }
    }

    private void handlePaymentFailed(Payment payment) {
        paymentMapper.update(
                null,
                new LambdaUpdateWrapper<Payment>()
                        .eq(
                                Payment::getPaymentNo,
                                payment.getPaymentNo()
                        )
                        .eq(
                                Payment::getStatus,
                                PaymentStatus.PENDING.getCode()
                        )
                        .set(
                                Payment::getStatus,
                                PaymentStatus.FAILED.getCode()
                        )
        );
    }

    private void checkSign(PaymentCallbackDTO dto) {
        if (!MOCK_SIGN.equals(dto.getSign())) {
            throw new BusinessException(ErrorCode.PAYMENT_SIGN_INVALID);
        }
    }

    private OrderDTO getOrder(String orderNo) {
        try {
            Result<OrderDTO> result = orderClient.getOrder(orderNo);
            if (result == null || result.getData() == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
            }

            return result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用order-service查询订单失败, orderNo={}", orderNo, e);
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }
    }

    private PaymentCreateVO buildPaymentCreateVO(Payment payment) {
        String payUrl = "mockpay://payment/" + payment.getPaymentNo() + "?channel=" + payment.getPayChannel();
        return new PaymentCreateVO(payment.getPaymentNo(), payUrl);
    }
}
