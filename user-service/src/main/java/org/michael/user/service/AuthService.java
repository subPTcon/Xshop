package org.michael.user.service;

import org.michael.user.dto.LoginRequest;
import org.michael.user.dto.LoginResponse;
import org.michael.user.dto.RegisterRequest;

public interface AuthService {

    Long register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    /**
     * 登出：使传入的token在Redis中失效
     * 设计为幂等操作--即使token已经不存在（重复登出/token已过期），也不报错，直接返回成功
     * 这是登出接口的通用语义：调用方只关心“登出后这个token确实用不了了“，而不关心它之前是否还存在
     *
     * @param rawAuthorizationHeader 原始的 Authorization请求头内容，可能带 "Bearer " 前缀，也可能不带
     */
    void logout(String rawAuthorizationHeader);
}
