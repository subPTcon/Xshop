package org.michael.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.order.client.CartClient;
import org.michael.order.client.InventoryClient;
import org.michael.order.client.ProductClient;
import org.michael.order.client.UserClient;
import org.michael.order.client.dto.*;
import org.michael.order.constant.OrderRedisConstant;
import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.dto.OrderItemCreateDTO;
import org.michael.order.pojo.Order;
import org.michael.order.pojo.OrderItem;
import org.michael.order.service.OrderPersistenceService;
import org.michael.order.service.OrderService;
import org.michael.order.vo.OrderCreateVO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final StringRedisTemplate redisTemplate;

    private final UserClient userClient;

    private final ProductClient productClient;

    private final InventoryClient inventoryClient;

    private final CartClient cartClient;
    private final OrderPersistenceService orderPersistenceService;

    @Override
    public OrderCreateVO createOrder(Long userId, OrderCreateDTO dto) {
        // 1.校验并消费防重Token
        checkAndConsumeToken(userId, dto.getIdempotentToken());

        // 2.查询收获地址
        AddressDTO address = getAddress(userId, dto.getAddressId());

        // 3.整理订单项 skuId -> count
        Map<Long, Integer> itemCountMap = buildItemCountMap(dto.getItems());
        List<Long> skuIds = new ArrayList<>(itemCountMap.keySet());

        // 4.查询最新商品信息
        Map<Long, SkuDTO> skuMap = getSkuMap(skuIds);

        // 5.校验SKU + 计算总金额
        BigDecimal totalAmount = calculateTotalAmount(itemCountMap, skuMap);

        // 6.生成订单号
        String orderNo = IdWorker.getIdStr();

        // 记录已经成功锁库存的商品，如果后面失败，用于补偿释放
        List<InventoryReserveRequest> reservedItems = new ArrayList<>();

        try {
            // 7.逐个预占库存
            reserveInventory(orderNo, itemCountMap, reservedItems);

            // 8.构造订单主表
            Order order = buildOrder(orderNo, userId, totalAmount, address);

            // 9.构造订单明细快照
            List<OrderItem> orderItems = buildOrderItems(orderNo, itemCountMap, skuMap);

            // 10.本地事务保存订单
            orderPersistenceService.saveOrder(order, orderItems);

        } catch (Exception e) {
            /**
             * 库存已经预占，但订单创建失败
             * 释放已经锁住的库存
             */
            releaseInventory(reservedItems);
            throw e;
        }

        /**
         * 11.订单已经成功落库
         *
         * 删除购物车商品失败
         * 不应该把已经创建成功的订单判成失败
         */
        removeCartItems(userId, skuIds);

        return new OrderCreateVO(orderNo, totalAmount);
    }

    private void checkAndConsumeToken(Long userId, String token) {
        String key = OrderRedisConstant.tokenKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/order_token_check.lua"));
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                token
        );

        if (result == null || result == 0) {
            throw new BusinessException(ErrorCode.ORDER_TOKEN_EXPIRED);
        }
        if (result == -1) {
            throw new BusinessException(ErrorCode.ORDER_TOKEN_INVALID);
        }
    }

    private AddressDTO getAddress(Long userId, Long addressId) {
        try {
            Result<AddressDTO> result = userClient.getAddress(userId, addressId);
            if (result == null || result.getData() == null) {
                throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
            }
            return result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用user-service查询地址失败, userId={}, addressId={}", userId, addressId, e);
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }
    }

    private Map<Long, Integer> buildItemCountMap(List<OrderItemCreateDTO> items) {
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (OrderItemCreateDTO item: items) {
            result.merge(item.getSkuId(), item.getCount(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entry: result.entrySet()) {
            if (entry.getValue() > 99) {
                throw new BusinessException(ErrorCode.PARAM_INVALID);
            }
        }
        return result;
    }

    private Map<Long, SkuDTO> getSkuMap(List<Long> skuIds) {
        try {
            Result<List<SkuDTO>> result =
                    productClient.batchGetSkus(
                            new SkuBatchRequest(
                                    skuIds
                            )
                    );
            if (result == null || result.getData() == null) {
                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }

            return result.getData()
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    SkuDTO::getSkuId,
                                    Function.identity()
                            )
                    );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "调用product-service批量查询SKU失败, skuIds={}",
                    skuIds,
                    e
            );

            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
        }
    }

    private BigDecimal calculateTotalAmount(
            Map<Long, Integer> itemCountMap,
            Map<Long, SkuDTO> skuMap
    ) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry: itemCountMap.entrySet()) {
            Long skuId = entry.getKey();
            Integer count = entry.getValue();
            SkuDTO sku = skuMap.get(skuId);
            if (sku == null) {
                throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
            }

            if (!Integer.valueOf(1).equals(sku.getSkuStatus())) {
                throw new BusinessException(ErrorCode.SKU_NOT_AVAILABLE);
            }

            if (!Integer.valueOf(1).equals(sku.getProductStatus())) {
                throw new BusinessException(ErrorCode.SKU_NOT_AVAILABLE);
            }

            BigDecimal itemAmount = sku.getPrice().multiply(BigDecimal.valueOf(count));

            total = total.add(itemAmount);
        }

        return total;
    }

    private void reserveInventory(String orderNo, Map<Long, Integer> itemCountMap, List<InventoryReserveRequest> reservedItems) {
        for (Map.Entry<Long, Integer> entry: itemCountMap.entrySet()) {
            InventoryReserveRequest request = new InventoryReserveRequest(entry.getKey(), orderNo, entry.getValue());
            try {
                Result<Boolean> result = inventoryClient.reserve(request);
                if (result == null || result.getData() == null || !Boolean.TRUE.equals(result.getData())) {
                    throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
                }

                reservedItems.add(request);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("预占库存失败, orderNo={}, skuId={}", orderNo, entry.getKey(), e);
                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }
        }
    }

    private void releaseInventory(List<InventoryReserveRequest> reservedItems) {
        for (InventoryReserveRequest item: reservedItems) {
            try {
                inventoryClient.release(
                        new InventoryReleaseRequest(
                                item.getSkuId(),
                                item.getOrderNo()
                        )
                );
            } catch (Exception e) {
                /**
                 * 这里不能覆盖真正的订单异常
                 *
                 * 第一阶段先记录日志
                 * 后面引入MQ/补偿任务解决
                 */
                log.error("库存补偿释放失败, orderNo={}, skuId={}", item.getOrderNo(), item.getSkuId(), e);
            }
        }
    }

    private Order buildOrder(String orderNo, Long userId, BigDecimal totalAmount, AddressDTO address) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        String receiverAddress = address.getProvince()
                                + address.getCity()
                                + address.getDistrict()
                                + address.getDetailAddress();
        order.setReceiverAddress(receiverAddress);
        return order;
    }

    private List<OrderItem> buildOrderItems(String orderNo, Map<Long, Integer> itemCountMap, Map<Long, SkuDTO> skuMap) {
        List<OrderItem> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry: itemCountMap.entrySet()) {
            Long skuId = entry.getKey();
            SkuDTO sku = skuMap.get(skuId);
            OrderItem item = new OrderItem();
            item.setOrderNo(orderNo);
            item.setSkuId(skuId);
            item.setProductTitle(sku.getProductTitle());
            item.setSkuSpec(sku.getSpecJson());
            item.setPrice(sku.getPrice());
            item.setCount(entry.getValue());
            result.add(item);
        }
        return result;
    }

    private void removeCartItems(Long userId, List<Long> skuIds) {
        try {
            cartClient.removeItems(new CartRemoveItemsRequest(userId, skuIds));
        } catch (Exception e) {
            log.warn("订单创建成功，但删除购物车商品失败， userId={}, skuIds={}", userId, skuIds, e);
        }
    }
}
