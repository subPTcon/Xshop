package org.michael.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.context.UserContext;
import org.michael.common.result.Result;
import org.michael.payment.dto.PaymentCallbackDTO;
import org.michael.payment.dto.PaymentCreateDTO;
import org.michael.payment.service.PaymentService;
import org.michael.payment.vo.PaymentCreateVO;
import org.michael.payment.vo.PaymentDetailVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public Result<PaymentCreateVO> createPayment(
            @Valid
            @RequestBody
            PaymentCreateDTO dto
    ) {
        log.info("POST /payments/create dto={}, 时间:{}", dto, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(paymentService.createPayment(userId, dto));
    }

    @PostMapping("/callback")
    public Result<Boolean> callback(
            @Valid
            @RequestBody
            PaymentCallbackDTO dto
    ) {
        log.info("POST /payments/callback dto={}, 时间:{}", dto, LocalDateTime.now());
        return Result.ok(paymentService.handleCallback(dto));
    }

    @GetMapping("/{paymentNo}")
    public Result<PaymentDetailVO> getPayment(@PathVariable String paymentNo) {
        log.info("GET /payments/{paymentNo} paymentNo={}", paymentNo);
        Long userId = UserContext.getUserId();
        return Result.ok(paymentService.getPayment(userId, paymentNo));
    }
}
