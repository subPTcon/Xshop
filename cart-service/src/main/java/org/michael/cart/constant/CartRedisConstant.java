package org.michael.cart.constant;

public final class CartRedisConstant {

    private CartRedisConstant() {}

    /**
     * 购物车 Redis Key 前缀
     */
    public static final String CART_KEY_PREFIX = "cart:";

    /**
     * 购物车有效期：30天
     */
    public static final long CART_EXPIRE_DAYS = 30;

    /**
     * 单SKU最大购买数量
     */
    public static final int MAX_ITEM_COUNT = 99;

    /**
     * 单购物车最多SKU种类数
     */
    public static final int MAX_CART_ITEMS = 100;

    public static String cartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }
}
