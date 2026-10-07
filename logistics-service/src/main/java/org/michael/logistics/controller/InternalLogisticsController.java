package org.michael.logistics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.logistics.service.LogisticsService;
import org.michael.logistics.vo.InternalLogisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/internal/logistics")
@RequiredArgsConstructor
public class InternalLogisticsController {

    private final LogisticsService logisticsService;

    @GetMapping("/order/{orderNo}")
    public Result<InternalLogisticsVO> getByOrderNo(@PathVariable String orderNo) {
        log.info("GET /internal/logistics/order/{orderNo} orderNo={}, 时间:{}", orderNo, LocalDateTime.now());
        return Result.ok(logisticsService.getInternalByOrderNo(orderNo));
    }
}
