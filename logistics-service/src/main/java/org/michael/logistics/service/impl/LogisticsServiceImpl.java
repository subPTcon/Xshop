package org.michael.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.logistics.client.OrderClient;
import org.michael.logistics.client.dto.OrderDTO;
import org.michael.logistics.dto.LogisticsShipDTO;
import org.michael.logistics.enums.OrderStatus;
import org.michael.logistics.mapper.LogisticsMapper;
import org.michael.logistics.mapper.LogisticsTrackMapper;
import org.michael.logistics.pojo.Logistics;
import org.michael.logistics.service.LogisticsService;
import org.michael.logistics.vo.LogisticsShipVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final LogisticsMapper logisticsMapper;

    private final LogisticsTrackMapper logisticsTrackMapper;

    private final OrderClient orderClient;
    private final LogisticsPersistenceService logisticsPersistenceService;

    @Override
    public LogisticsShipVO ship(LogisticsShipDTO dto) {
        /**
         * 1.先根据 orderNo 查询物流单
         *
         * orderNo是幂等键
         */
        Logistics existing = findByOrderNo(dto.getOrderNo());
        if (existing != null) {
            /**
             * 已经创建过物流单
             *
             * 这里不要直接返回
             *
             * 因为有可能上一次：
             *
             * logistics DB成功，但是调用 order-service失败
             * 所以重试时再次通知订单服务
             *
             *
             */
            notifyOrderShipped(dto.getOrderNo());
            return new LogisticsShipVO(existing.getLogisticsNo(), true);
        }

        /**
         * 2.查询订单
         */
        OrderDTO order = getOrder(dto.getOrderNo());

        /**
         * 3.校验订单状态
         */
        if (!OrderStatus.PENDING_SHIPMENT.getCode().equals(order.getStatus())) {
            /**
             * 如果订单已经发货，但物流数据存在异常
             * 不应该重新创建
             */
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        /**
         * 4.生成物流单号
         */
        String logisticsNo = IdWorker.getIdStr();
        try {
            /**
             * 5.本地事务:
             *
             * 创建物流单 + 第一条物流轨迹
             */
            logisticsPersistenceService.createShipment(dto, logisticsNo);
        } catch (DuplicateKeyException e) {
            Logistics concurrent = findByOrderNo(dto.getOrderNo());
            if (concurrent == null) {
                throw e;
            }
            logisticsNo = concurrent.getLogisticsNo();
        }

        /**
         * 6.通知 order-service
         *
         * 2 待发货 -> 3 已发货
         */
        notifyOrderShipped(dto.getOrderNo());
        log.info("订单发货成功, orderNo={}, logisticsNo={}, carrier={}", dto.getOrderNo(), logisticsNo, dto.getCarrier());

        return new LogisticsShipVO(logisticsNo, true);
    }

    private OrderDTO getOrder(String orderNo) {
        try {
            Result<OrderDTO> result = orderClient.getOrder(orderNo);
            if (result == null || result.getData() == null) {
                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }

            return result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询order-service失败, orderNo={}", orderNo, e);
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }
    }

    private void notifyOrderShipped(String orderNo) {
        try {
            Result<Boolean> result = orderClient.markShipped(orderNo);
            if (result == null || result.getData() == null || !Boolean.TRUE.equals(result.getData())) {
                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("通知order-service发货失败，orderNo={}", orderNo, e);
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }
    }

    private Logistics findByOrderNo(String orderNo) {
        return logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>()
                        .eq(
                                Logistics::getOrderNo,
                                orderNo
                        )
        );
    }
}
