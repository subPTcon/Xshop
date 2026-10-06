package org.michael.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.order.service.OrderService;
import org.michael.order.vo.InternalOrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @GetMapping("/{orderNo}")
    public Result<InternalOrderVO> getOrder(
            @PathVariable
            String orderNo
    ) {
        log.info("GET /internal/orders/{orderNo} orderNo={}, 时间:{}", orderNo, LocalDateTime.now());
        return Result.ok(orderService.getInternalOrder(orderNo));
    }
}
