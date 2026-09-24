package org.michael.xshop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.xshop.common.result.Result;
import org.michael.xshop.dto.UserBasicInfo;
import org.michael.xshop.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 *  内部接口，专供其他微服务通过 Feign 调用，不面向前端/外部客户端
 *  路径统一以 /internal 开头：
 *   1. 网关层面据此统一屏蔽外部直接访问这一类路径
 *   2. 本地也据此把它从LoginInterceptor的校验范围里排出掉，服务间调用不会携带用户的JWT
 *      要求它带登录状态是没有意义的
 */
@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Result<UserBasicInfo> getUserBasicInfo(@PathVariable Long id) {
        log.info("GET /internal/users/{} 时间：{}", id, LocalDateTime.now());
        return Result.ok(userService.getUserBasicInfo(id));
    }
}
