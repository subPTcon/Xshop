package org.michael.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.context.UserContext;
import org.michael.common.result.Result;
import org.michael.order.dto.OrderCancelDTO;
import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.service.OrderService;
import org.michael.order.vo.OrderCreateVO;
import org.michael.order.vo.OrderDetailVO;
import org.michael.order.vo.OrderPageVO;
import org.michael.order.vo.OrderTokenVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<OrderCreateVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        log.info("POST /orders/create dto={}, 时间:{}", dto, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(orderService.createOrder(userId, dto));
    }

    @GetMapping("/token")
    public Result<OrderTokenVO> getOrderToken() {
        log.info("GET /orders/token 时间:{}", LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(orderService.generateOrderToken(userId));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> getOrderDetail(
            @PathVariable
            String orderNo
    ) {
        log.info("GET /orders/{orderNo} orderNo={}, 时间:{}", orderNo, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(orderService.getOrderDetail(userId, orderNo));
    }

    @GetMapping
    public Result<OrderPageVO> getOrderList(
            @RequestParam(required = false)
            Integer status,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码必须大于0")
            Integer page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页最多100条")
            Integer size
    ) {
        log.info("GET /orders status={}, page={}, size={}, 时间:{}", status, page, size, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(orderService.getOrderList(userId, status, page, size));
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Boolean> cancelOrder(
            @PathVariable
            String orderNo,

            @Valid
            @RequestBody
            OrderCancelDTO dto
    ) {
        log.info("POST /orders/cancel/{orderNo} orderNo={}, dto={}, 时间:{}", orderNo, dto, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(orderService.cancelOrder(userId, orderNo, dto));
    }
}
