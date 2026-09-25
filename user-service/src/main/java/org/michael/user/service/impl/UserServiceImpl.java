package org.michael.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.user.dto.UserBasicInfo;
import org.michael.user.dto.UserInfoResponse;
import org.michael.user.mapper.UserMapper;
import org.michael.user.pojo.User;
import org.michael.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);

        // 理论上走到这里userId必然来自有效token，对应用户应该必然存在
        // 但用户注销、账号被后台删除等边缘情况仍可能发生，兜底判空避免空指针
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        return response;
    }

    @Override
    public UserBasicInfo getUserBasicInfo(Long userId) {
        User user = userMapper.selectById(userId);

        // 调用方是order-service等其他服务，它们传来的userId
        // 可能是脏数据、已删除用户的历史ID等，必须明确报错而不是返回一个字段全空的对象
        // 让调用方在 Feign 层面就能感知到“这个用户查不到”，而不是拿到一个看似正常但没数据的响应
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserBasicInfo basicInfo = new UserBasicInfo();
        basicInfo.setId(user.getId());
        basicInfo.setUsername(user.getUsername());
        basicInfo.setNickname(user.getNickname());
        return basicInfo;
    }


}
