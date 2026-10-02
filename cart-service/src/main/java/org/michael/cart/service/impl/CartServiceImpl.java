package org.michael.cart.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.cart.client.ProductClient;
import org.michael.cart.client.dto.SkuDTO;
import org.michael.cart.constant.CartRedisConstant;
import org.michael.cart.dto.CartItemAddDTO;
import org.michael.cart.dto.CartItemUpdateDTO;
import org.michael.cart.service.CartService;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> cartAddScript;
    private final DefaultRedisScript<Long> cartUpdateScript;

    private final ProductClient productClient;

    @Override
    public Boolean addItem(Long userId, CartItemAddDTO dto) {
        Long skuId = dto.getSkuId();

        // 1.校验SKU
        validateSku(skuId);

        // 2.构造Redis key
        String cartKey = CartRedisConstant.cartKey(userId);

        // 3.Lua原子添加
        Long result = redisTemplate.execute(
                cartAddScript,
                List.of(cartKey),
                String.valueOf(skuId),
                String.valueOf(dto.getCount()),
                String.valueOf(CartRedisConstant.MAX_ITEM_COUNT),
                String.valueOf(CartRedisConstant.MAX_CART_ITEMS),
                String.valueOf(Duration.ofDays(CartRedisConstant.CART_EXPIRE_DAYS).toSeconds())
        );

        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 单SKU数量超过限制
        if (result == -1L) {
            throw new BusinessException(ErrorCode.CART_ITEM_COUNT_EXCEEDED);
        }

        // SKU种类超过限制
        if (result == -2L) {
            throw new BusinessException(ErrorCode.CART_ITEM_LIMIT_EXCEEDED);
        }

        log.info("添加购物车成功，userId={}, skuId={}, addCount={}, finalCount={}", userId, skuId, dto.getCount(), result);

        return Boolean.TRUE;
    }

    @Override
    public Boolean updateItem(Long userId, Long skuId, CartItemUpdateDTO dto) {
        String cartKey = CartRedisConstant.cartKey(userId);
        Long result = redisTemplate.execute(
                cartUpdateScript,
                List.of(cartKey),
                String.valueOf(skuId),
                String.valueOf(dto.getCount()),
                String.valueOf(Duration.ofDays(CartRedisConstant.CART_EXPIRE_DAYS).toSeconds())
        );

        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        if (result == 0L) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        log.info("修改购物车商品数量成功，userId={}, skuId={}, count={}", userId, skuId, dto.getCount());

        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteItem(Long userId, Long skuId) {
        String cartKey = CartRedisConstant.cartKey(userId);
        Long deleted = redisTemplate.opsForHash().delete(cartKey, String.valueOf(skuId));
        log.info("删除购物车商品，userId={}, skuId={}, deleted={}", userId, skuId, deleted);
        return Boolean.TRUE;
    }

    private void validateSku(Long skuId) {
        Result<SkuDTO> result;

        try {
            result = productClient.getSkuById(skuId);
        } catch (Exception e) {
            log.error("调用product-service查询SKU失败, skuId={}", skuId, e);

            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }

        if (result == null || result.getData() == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        SkuDTO sku = result.getData();
        if (!Integer.valueOf(1).equals(sku.getStatus())) {
            throw new BusinessException(ErrorCode.SKU_NOT_AVAILABLE);
        }
    }

}
