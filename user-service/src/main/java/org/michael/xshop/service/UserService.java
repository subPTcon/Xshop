package org.michael.xshop.service;

import org.michael.xshop.dto.UserInfoResponse;

public interface UserService {

    /**
     * 根据用户ID查询用户信息
     */
    UserInfoResponse getUserInfo(Long userId);
}
