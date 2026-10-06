package org.michael.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.payment.client.OrderClient;
import org.michael.payment.client.dto.OrderDTO;
import org.michael.payment.dto.PaymentCreateDTO;
import org.michael.payment.mapper.PaymentMapper;
import org.michael.payment.pojo.Payment;
import org.michael.payment.vo.PaymentCreateVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private final PaymentMapper paymentMapper;

    private final OrderClient orderClient;

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
