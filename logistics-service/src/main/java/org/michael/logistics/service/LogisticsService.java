package org.michael.logistics.service;

import org.michael.logistics.dto.LogisticsShipDTO;
import org.michael.logistics.dto.LogisticsUpdateDTO;
import org.michael.logistics.vo.InternalLogisticsVO;
import org.michael.logistics.vo.LogisticsDetailVO;
import org.michael.logistics.vo.LogisticsShipVO;

public interface LogisticsService {

    LogisticsShipVO ship(LogisticsShipDTO dto);

    Boolean update(String logisticsNo, LogisticsUpdateDTO dto);

    LogisticsDetailVO getByOrderNo(Long userId, String orderNo);

    InternalLogisticsVO getInternalByOrderNo(String orderNo);
}
