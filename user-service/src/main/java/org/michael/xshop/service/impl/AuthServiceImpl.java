package org.michael.xshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.xshop.common.exception.BusinessException;
import org.michael.xshop.common.exception.ErrorCode;
import org.michael.xshop.dto.LoginRequest;
import org.michael.xshop.dto.LoginResponse;
import org.michael.xshop.dto.RegisterRequest;
import org.michael.xshop.mapper.UserMapper;
import org.michael.xshop.pojo.User;
import org.michael.xshop.service.AuthService;
import org.michael.xshop.util.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final Duration LOGIN_FAIL_EXPIRE = Duration.ofMinutes(10);
    private static final Duration TOKEN_EXPIRE = Duration.ofHours(2);
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

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

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        String failKey = "login:fail:" + username;

        // 1.检查当前账号失败次数
        String failCountStr = redisTemplate.opsForValue().get(failKey);

        if (failCountStr != null && Integer.parseInt(failCountStr) >= MAX_LOGIN_FAIL_COUNT) {
            throw new BusinessException(ErrorCode.LOGIN_TOO_MANY_ATTEMPTS);
        }

        // 2.查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );

        // 3.用户不存在/密码错误
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLoginFailure(failKey);
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 4.检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 5.登录成功，清除失败次数
        redisTemplate.delete(failKey);

        // 6.生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 7.保存登录态
        String tokenKey = "token:" + token;

        redisTemplate.opsForValue().set(
                tokenKey,
                String.valueOf(user.getId()),
                TOKEN_EXPIRE
        );

        // 8.返回
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpireIn(jwtUtil.getExpireSeconds());
        response.setUserId(user.getId());

        return response;
    }

    private void recordLoginFailure(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);

        if (count != null && count == 1) {
            redisTemplate.expire(failKey, LOGIN_FAIL_EXPIRE);
        }
    }

    @Override
    public void logout(String rawAuthorizationHeader) {
        String token = extractToken(rawAuthorizationHeader);
        // 没带token或格式不对，直接当成已经是登出状态，不报错一登出接口的语义就是“确保这个token失效”
        // 一个本来就没有效token的请求，结果也是“已失效"，没必要额外报UNAUTHORIZED打断调用方
        if (token == null) {
            return;
        }

        Boolean deleted = redisTemplate.delete(token);
        log.info("用户登出，tokenPrefix={}..., redisKeyDeleted={}", token.length() > 8 ? token.substring(0, 8) : token, deleted);
    }

    /**
     * 从 Authorization 请求头里提取真正的token值，兼容带"Bearer " 前缀和不带前缀两种情况
     *
     * 返回null代表这个请求头本身就是无效/缺失的
     */
    private String extractToken(String rawAuthorizationHeader) {
        if (!StringUtils.hasText(rawAuthorizationHeader)) {
            return null;
        }

        if (rawAuthorizationHeader.startsWith(BEARER_PREFIX)) {
            return rawAuthorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }

        return rawAuthorizationHeader.trim();
    }
}
