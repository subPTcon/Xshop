package org.michael.order.constant;

public final class OrderRedisConstant {

    private OrderRedisConstant() {}

    public static final String ORDER_TOKEN_PREFIX = "order:token:";

    public static String tokenKey(Long userId) {
        return ORDER_TOKEN_PREFIX + userId;
    }
}
