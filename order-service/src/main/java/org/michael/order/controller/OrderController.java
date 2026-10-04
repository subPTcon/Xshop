package org.michael.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.context.UserContext;
import org.michael.common.result.Result;
import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.service.OrderService;
import org.michael.order.vo.OrderCreateVO;
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
}
