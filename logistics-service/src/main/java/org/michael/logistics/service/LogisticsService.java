package org.michael.logistics.service;

import org.michael.logistics.dto.LogisticsShipDTO;
import org.michael.logistics.vo.LogisticsShipVO;

public interface LogisticsService {

    LogisticsShipVO ship(LogisticsShipDTO dto);
}
