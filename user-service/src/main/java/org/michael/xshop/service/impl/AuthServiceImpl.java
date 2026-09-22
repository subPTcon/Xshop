package org.michael.xshop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.michael.xshop.common.exception.BusinessException;
import org.michael.xshop.common.exception.ErrorCode;
import org.michael.xshop.dto.RegisterRequest;
import org.michael.xshop.mapper.UserMapper;
import org.michael.xshop.pojo.User;
import org.michael.xshop.service.AuthService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Long register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String phone = request.getPhone().trim();
        String password = request.getPassword();

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(
                    ErrorCode.PARAM_INVALID,
                    "密码的UTF-8编码长度不能超过72个字节"
            );
        }

        Long usernameCount = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username)
        );

        if (usernameCount > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        Long phoneCount = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, phone)
        );

        if (phoneCount > 0) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setStatus(1);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.REGISTER_CONFLICT);
        }

        return user.getId();
    }
}
