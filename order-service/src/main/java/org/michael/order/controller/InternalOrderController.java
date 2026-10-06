package org.michael.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.order.dto.OrderPaidDTO;
import org.michael.order.service.OrderService;
import org.michael.order.vo.InternalOrderVO;
import org.springframework.web.bind.annotation.*;

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

//    @PostMapping("/{orderNo}/paid")
//    public Result<Boolean> markPaid(
//            @PathVariable String orderNo,
//            @RequestBody OrderPaidDTO dto
//    ) {
//        log.info("POST /internal/orders/{orderNo}/paid orderNo={}, dto={}, 时间:{}", orderNo, dto, LocalDateTime.now());
//        return Result.ok(orderService.markPaid(orderNo, dto.getPayTime()));
//    }

    @PostMapping("/{orderNo}/shipped")
    public Result<Boolean> markShipped(@PathVariable String orderNo) {
        log.info("POST /internal/orders/{orderNo}/shipped orderNo={}, 时间:{}", orderNo, LocalDateTime.now());
        return Result.ok(orderService.markShipped(orderNo));
    }

    @PostMapping("/{orderNo}/payment-success")
    public Result<Boolean> paymentSuccess(
            @PathVariable String orderNo
    ) {
        log.info("POST /internal/orders/{orderNo}/payment-success orderNo={}, 时间:{}", orderNo, LocalDateTime.now());
        return Result.ok(orderService.handlePaymentSuccess(orderNo));
    }
}
