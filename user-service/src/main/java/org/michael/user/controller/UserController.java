package org.michael.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.user.common.context.UserContext;
import org.michael.user.dto.UserInfoResponse;
import org.michael.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        log.info("POST /users/me 时间:{}", LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(userService.getUserInfo(userId));
    }
}
