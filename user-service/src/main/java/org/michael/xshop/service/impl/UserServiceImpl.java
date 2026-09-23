package org.michael.xshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.michael.xshop.common.exception.BusinessException;
import org.michael.xshop.common.exception.ErrorCode;
import org.michael.xshop.dto.UserInfoResponse;
import org.michael.xshop.mapper.UserMapper;
import org.michael.xshop.pojo.User;
import org.michael.xshop.service.UserService;
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
}
