package org.michael.logistics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.logistics.dto.LogisticsShipDTO;
import org.michael.logistics.service.LogisticsService;
import org.michael.logistics.vo.LogisticsShipVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/admin/logistics")
@RequiredArgsConstructor
public class AdminLogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping("/ship")
    public Result<LogisticsShipVO> ship(
            @Valid
            @RequestBody
            LogisticsShipDTO dto
    ) {
        log.info("POST /admin/logistics/ship dto={}, 时间:{}", dto, LocalDateTime.now());
        return Result.ok(logisticsService.ship(dto));
    }
}
