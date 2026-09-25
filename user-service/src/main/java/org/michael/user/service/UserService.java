package org.michael.user.service;

import org.michael.user.dto.UserBasicInfo;
import org.michael.user.dto.UserInfoResponse;

public interface UserService {

    /**
     * 根据用户ID查询用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 根据用户ID查询用户基础信息（服务间调用专用，Feign场景，字段比getUserInfo更精简）
     */
    UserBasicInfo getUserBasicInfo(Long userId);
}
