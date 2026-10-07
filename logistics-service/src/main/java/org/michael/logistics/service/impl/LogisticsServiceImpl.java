package org.michael.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.logistics.client.OrderClient;
import org.michael.logistics.dto.LogisticsShipDTO;
import org.michael.logistics.dto.LogisticsUpdateDTO;
import org.michael.logistics.enums.LogisticsStatus;
import org.michael.logistics.mapper.LogisticsMapper;
import org.michael.logistics.mapper.LogisticsTrackMapper;
import org.michael.logistics.pojo.Logistics;
import org.michael.logistics.pojo.LogisticsTrack;
import org.michael.logistics.service.LogisticsService;
import org.michael.logistics.vo.LogisticsShipVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
         * 1.先查是否已经有物流订单
         */
        Logistics existing = findByOrderNo(dto.getOrderNo());
        if (existing != null) {
            /**
             * 已经创建过物流单
             *
             * 再通知一次 order-service
             * 因为上一次有可能:
             *
             * logistics 写成功
             * order-service 调用失败
             */
            notifyOrderShipped(dto.getOrderNo());
            return new LogisticsShipVO(existing.getLogisticsNo(), true);
        }

        /**
         * 2.生成物流单号
         */
        String logisticsNo = IdWorker.getIdStr();
        try {
            /**
             * 3.本地事务：
             * 创建物流单 + 第一条轨迹
             */
            logisticsPersistenceService.createShipment(dto, logisticsNo);
        } catch (DuplicateKeyException e) {
            /**
             * 并发请求兜底
             *
             * orderNo 上有 UNIQUE
             */
            Logistics concurrent = findByOrderNo(dto.getOrderNo());
            if (concurrent == null) {
                throw e;
            }

            logisticsNo = concurrent.getLogisticsNo();
        }

        /**
         * 4.通知 order-service
         *
         * PENDING_SHIPMENT -> SHIPPED
         */
        notifyOrderShipped(dto.getOrderNo());

        log.info("订单发货成功, orderNo={}, logisticsNo={}", dto.getOrderNo(), logisticsNo);

        return new LogisticsShipVO(logisticsNo, true);
    }

    @Override
    @Transactional
    public Boolean update(String logisticsNo, LogisticsUpdateDTO dto) {
        /**
         * 1.查询物流单
         */
        Logistics logistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>()
                        .eq(
                                Logistics::getLogisticsNo,
                                logisticsNo
                        )
        );
        if (logistics == null) {
            throw new BusinessException(ErrorCode.LOGISTICS_NOT_FOUND);
        }

        /**
         * 2.重复更新，直接成功
         */
        if (dto.getStatus().equals(logistics.getStatus())) {
            return Boolean.TRUE;
        }

        /**
         * 3.校验状态流转
         */
        if (!LogisticsStatus.canTransit(logistics.getStatus(), dto.getStatus())) {
            throw new BusinessException(ErrorCode.LOGISTICS_STATUS_INVALID);
        }

        /**
         * 4.条件更新物流主表
         */
        int affected = logisticsMapper.update(
                null,
                new LambdaUpdateWrapper<Logistics>()
                        .eq(
                                Logistics::getLogisticsNo,
                                logisticsNo
                        )
                        .eq(
                                Logistics::getStatus,
                                logistics.getStatus()
                        )
                        .set(
                                Logistics::getStatus,
                                dto.getStatus()
                        )
                        .set(
                                Logistics::getCurrentLocation,
                                dto.getLocation()
                        )
        );

        if (affected == 0) {
            throw new BusinessException(ErrorCode.LOGISTICS_STATUS_INVALID);
        }

        /**
         * 5.新增物流轨迹
         */
        LogisticsTrack track = new LogisticsTrack();
        track.setLogisticsNo(logisticsNo);
        track.setLocation(dto.getLocation());
        track.setDescription(dto.getDescription());
        track.setTrackTime(LocalDateTime.now());
        logisticsTrackMapper.insert(track);

        return Boolean.TRUE;
    }

    private void notifyOrderShipped(String orderNo) {
        try {
            Result<Boolean> result = orderClient.markShipped(orderNo);
            if (result == null || !Boolean.TRUE.equals(result.getData())) {
                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("通知 order-service 发货失败，orderNo={}", orderNo, e);
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
