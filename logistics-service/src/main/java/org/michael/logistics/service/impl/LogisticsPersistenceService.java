package org.michael.logistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.michael.logistics.dto.LogisticsShipDTO;
import org.michael.logistics.enums.LogisticsStatus;
import org.michael.logistics.mapper.LogisticsMapper;
import org.michael.logistics.mapper.LogisticsTrackMapper;
import org.michael.logistics.pojo.Logistics;
import org.michael.logistics.pojo.LogisticsTrack;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogisticsPersistenceService {

    private final LogisticsMapper logisticsMapper;

    private final LogisticsTrackMapper logisticsTrackMapper;

    @Transactional
    public Logistics createShipment(LogisticsShipDTO dto, String logisticsNo) {
        Logistics logistics = new Logistics();
        logistics.setLogisticsNo(logisticsNo);
        logistics.setOrderNo(dto.getOrderNo());
        logistics.setCarrier(dto.getCarrier());
        logistics.setStatus(LogisticsStatus.SHIPPED.getCode());
        logistics.setCurrentLocation(dto.getLocation());
        logisticsMapper.insert(logistics);

        /**
         * 第一条物流轨迹
         */
        LogisticsTrack track = new LogisticsTrack();
        track.setLogisticsNo(logisticsNo);
        track.setLocation(dto.getLocation());
        track.setDescription(dto.getDescription());
        track.setTrackTime(LocalDateTime.now());
        logisticsTrackMapper.insert(track);

        return logistics;
    }


}
