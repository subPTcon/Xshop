package org.michael.xshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.xshop.common.result.Result;
import org.michael.xshop.dto.LoginRequest;
import org.michael.xshop.dto.LoginResponse;
import org.michael.xshop.dto.RegisterRequest;
import org.michael.xshop.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register 请求时间：{}", LocalDateTime.now());
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login 请求时间：{}", LocalDateTime.now());
        return Result.ok(authService.login(request));
    }
}
