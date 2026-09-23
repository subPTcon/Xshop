package org.michael.xshop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.xshop.common.context.UserContext;
import org.michael.xshop.common.result.Result;
import org.michael.xshop.dto.UserInfoResponse;
import org.michael.xshop.service.UserService;
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
