package org.michael.order.constant;

public final class OrderRedisConstant {

    private OrderRedisConstant() {}

    public static final String ORDER_TOKEN_PREFIX = "order:token:";

    public static final long ORDER_TOKEN_EXPIRE_MINUTES = 10;

    public static String tokenKey(Long userId) {
        return ORDER_TOKEN_PREFIX + userId;
    }
}
