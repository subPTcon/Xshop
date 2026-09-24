package org.michael.xshop.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.michael.xshop.common.context.UserContext;
import org.michael.xshop.common.exception.BusinessException;
import org.michael.xshop.common.exception.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录态校验拦截器
 * 拦截所有标记为需要登录的接口，从Authorization头里取token，去Redis查对应的userId
 * 查到了就存进UserContext供后续Controller/Service使用，查不到就直接抛异常，请求根本进不了Controller
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request.getHeader("Authorization"));
        System.out.println("TOKEN: " + token);

        if (token == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String userIdStr = redisTemplate.opsForValue().get("token:" + token);
        System.out.println("userIdStr:" + userIdStr);
        if (userIdStr == null) {
            // Redis里查不到：要么token从没登录过，要么已经登出/自然过期，统一按同一种错误提示
            // 不区分“过期”和“无效”两种情况
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        UserContext.setUserId(Long.valueOf(userIdStr));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 无论请求成功、失败还是抛异常，都必须清理 ThreadLocal，防止线程池复用导致用户信息串线
        UserContext.clear();
    }

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
