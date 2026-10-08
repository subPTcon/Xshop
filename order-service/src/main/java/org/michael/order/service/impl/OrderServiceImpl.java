package org.michael.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.michael.order.component.OrderStateMachine;
import org.michael.order.constant.OrderRedisConstant;
import org.michael.order.dto.OrderCancelDTO;
import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.dto.OrderItemCreateDTO;
import org.michael.order.enums.OrderStatus;
import org.michael.order.event.OrderCancelledEvent;
import org.michael.order.event.OrderCreatedEvent;
import org.michael.order.event.OrderEventProducer;
import org.michael.order.mapper.OrderItemMapper;
import org.michael.order.mapper.OrderMapper;
import org.michael.order.pojo.Order;
import org.michael.order.pojo.OrderItem;
import org.michael.order.service.OrderPersistenceService;
import org.michael.order.service.OrderService;
import org.michael.order.vo.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CartClient cartClient;
    private final OrderPersistenceService orderPersistenceService;
    private final OrderStateMachine orderStateMachine;
    private final OrderEventProducer orderEventProducer;

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

            OrderCreatedEvent event = new OrderCreatedEvent(
                    UUID.randomUUID().toString(),
                    orderNo,
                    userId,
                    totalAmount
            );

            orderEventProducer.sendOrderCreated(event);

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

    @Override
    public OrderTokenVO generateOrderToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key = OrderRedisConstant.tokenKey(userId);
        redisTemplate.opsForValue()
                .set(
                        key,
                        token,
                        OrderRedisConstant.ORDER_TOKEN_EXPIRE_MINUTES,
                        TimeUnit.MINUTES
                );
        log.info("生成下单防重Token, userId={}", userId);
        return new OrderTokenVO(token);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long userId, String orderNo) {
        // 1.查订单
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
                        .eq(
                                Order::getUserId,
                                userId
                        )
        );
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2.查询订单明细
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(
                                OrderItem::getOrderNo,
                                orderNo
                        )
                        .orderByAsc(
                                OrderItem::getId
                        )
        );

        // 3.组装VO
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setPayTime(order.getPayTime());
        vo.setCreateTime(order.getCreateTime());
        List<OrderItemVO> itemVOList = orderItems.stream()
                .map(item -> {
                    OrderItemVO itemVO = new OrderItemVO();
                    itemVO.setSkuId(item.getSkuId());
                    itemVO.setProductTitle(item.getProductTitle());
                    itemVO.setSkuSpec(item.getSkuSpec());
                    itemVO.setPrice(item.getPrice());
                    itemVO.setCount(item.getCount());
                    return itemVO;
                }).toList();

        vo.setItems(itemVOList);
        return vo;
    }

    @Override
    public OrderPageVO getOrderList(
            Long userId,
            Integer status,
            Integer page,
            Integer size
    ) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(
                        Order::getUserId,
                        userId
                )
                .orderByDesc(
                        Order::getCreateTime
                );

        if (status != null) {
            wrapper.eq(
                    Order::getStatus,
                    status
            );
        }
        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
        List<OrderListItemVO> list = orderPage.getRecords()
                .stream()
                .map(order -> {
                    OrderListItemVO vo = new OrderListItemVO();
                    vo.setOrderNo(order.getOrderNo());
                    vo.setTotalAmount(order.getTotalAmount());
                    vo.setStatus(order.getStatus());
                    vo.setCreateTime(order.getCreateTime());

                    return vo;
                }).toList();

        return new OrderPageVO(list, orderPage.getTotal());
    }

    @Override
    public Boolean cancelOrder(Long userId, String orderNo, OrderCancelDTO dto) {
        // 1.查询订单
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
                        .eq(
                                Order::getUserId,
                                userId
                        )

        );
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        /**
         * 2.已经取消
         *
         * 接口做成幂等
         *
         * 这里仍然重新执行一次库存释放
         * 因为 inventory release 本身也应该幂等
         *
         * 如果上一次订单状态改成5成功，但是库存释放失败，用户再次取消就可以顺便补偿
         */
        if (OrderStatus.CANCELLED.getCode().equals(order.getStatus())) {
            releaseOrderInventory(orderNo);
            return Boolean.TRUE;
        }

        // 3.检查状态机
        orderStateMachine.checkCancel(order.getStatus());

        // 4.条件更新，只有 status = 0 才更新成5
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<Order>()
                .eq(
                        Order::getOrderNo,
                        orderNo
                )
                .eq(
                        Order::getUserId,
                        userId
                )
                .eq(
                        Order::getStatus,
                        OrderStatus.PENDING_PAYMENT.getCode()
                )
                .set(
                        Order::getStatus,
                        OrderStatus.CANCELLED.getCode()
                )
                .set(
                        Order::getCancelReason,
                        dto.getReason()
                );
        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                order.getOrderNo(),
                order.getUserId(),
                dto.getReason()
        );
        orderEventProducer.sendOrderCancelled(event);

        int affected = orderMapper.update(null, wrapper);

        // 5.affected = 0 说明发生了并发状态变化
        if (affected == 0) {
            Order latestOrder = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>()
                            .eq(
                                    Order::getOrderNo,
                                    orderNo
                            )
                            .eq(
                                    Order::getUserId,
                                    userId
                            )
            );

            if (latestOrder != null && OrderStatus.CANCELLED.getCode().equals(latestOrder.getStatus())) {
                releaseOrderInventory(orderNo);
                return Boolean.TRUE;
            }

            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        // 6.状态修改成功，释放预占库存
        releaseOrderInventory(orderNo);
        log.info("订单取消成功, userId={}, orderNo={}, reason={}", userId, orderNo, dto.getReason());

        return Boolean.TRUE;
    }

    private void releaseOrderInventory(String orderNo) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(
                                OrderItem::getOrderNo,
                                orderNo
                        )
        );
        for (OrderItem item: items) {
            try {
                Result<Boolean> result = inventoryClient.release(
                        new InventoryReleaseRequest(
                                item.getSkuId(),
                                orderNo
                        )
                );

                if (result == null || result.getData() == null || !Boolean.TRUE.equals(result.getData())) {
                    throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("取消订单释放库存失败, orderNo={}, skuId={}", orderNo, item.getSkuId());

                throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR);
            }
        }
    }

    @Override
    public Boolean confirmOrder(Long userId, String orderNo) {
        // 1.查询订单
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
                        .eq(
                                Order::getUserId,
                                userId
                        )
        );
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2.已完成，直接返回成功，保证接口幂等
        if (OrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            return Boolean.TRUE;
        }

        // 3.校验状态，只有已发货才能确认收货
        orderStateMachine.checkConfirm(order.getStatus());

        /**
         * 4.条件更新
         *
         * 只有数据库当前仍然是 SHIPPED(3)
         * 才允许变成COMPLETED(4)
         */
        int affected = orderMapper.update(
                null,
                new LambdaQueryWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
                        .eq(
                                Order::getUserId,
                                userId
                        )
                        .eq(
                                Order::getStatus,
                                OrderStatus.SHIPPED.getCode()
                        )
                        .eq(
                                Order::getStatus,
                                OrderStatus.COMPLETED.getCode()
                        )
        );

        // 5.更新失败说明状态发生并发变化
        if (affected == 0) {
            Order latestOrder = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>()
                            .eq(
                                    Order::getOrderNo,
                                    orderNo
                            )
                            .eq(
                                    Order::getUserId,
                                    userId
                            )
            );

            // 如果另一个确认请求已经成功，当前请求仍然返回成功
            if (latestOrder != null && OrderStatus.COMPLETED.getCode().equals(latestOrder.getStatus())) {
                return Boolean.TRUE;
            }

            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        log.info("订单确认收货成功, userId={}, orderNo={}", userId, orderNo);
        return Boolean.TRUE;
    }

    @Override
    public InternalOrderVO getInternalOrder(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
        );

        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        InternalOrderVO vo = new InternalOrderVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        return vo;
    }

    @Override
    public Boolean markShipped(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
        );
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 已经发货，重复调用，直接返回成功
        if (OrderStatus.SHIPPED.getCode().equals(order.getStatus())) {
            return Boolean.TRUE;
        }

        if (!OrderStatus.PENDING_SHIPMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        int affected = orderMapper.update(
                null,
                new LambdaUpdateWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
                        .eq(
                                Order::getStatus,
                                OrderStatus.PENDING_SHIPMENT.getCode()
                        )
                        .set(
                                Order::getStatus,
                                OrderStatus.SHIPPED.getCode()
                        )
        );
        if (affected == 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean handlePaymentSuccess(String orderNo) {

        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(
                                Order::getOrderNo,
                                orderNo
                        )
        );

        if (order == null) {
            throw new BusinessException(
                    ErrorCode.ORDER_NOT_FOUND
            );
        }

        /*
         * 重复支付通知
         */
        if (OrderStatus.PENDING_SHIPMENT
                .getCode()
                .equals(order.getStatus())) {

            return Boolean.TRUE;
        }

        /*
         * 只有待支付订单才能处理支付成功
         */
        if (!OrderStatus.PENDING_PAYMENT
                .getCode()
                .equals(order.getStatus())) {

            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        /*
         * 1. 查询订单商品
         */
        List<OrderItem> items =
                orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>()
                                .eq(
                                        OrderItem::getOrderNo,
                                        orderNo
                                )
                );

        /*
         * 2. 确认库存
         *
         * RESERVED -> CONFIRMED
         */
        for (OrderItem item : items) {

            Boolean success =
                    inventoryClient.confirm(new InventoryConfirmRequest(item.getSkuId(), item.getOrderNo())
                    ).getData();

            if (!Boolean.TRUE.equals(success)) {

                throw new BusinessException(
                        ErrorCode.REMOTE_SERVICE_ERROR
                );
            }
        }

        /*
         * 3. 所有库存确认成功以后，
         * 再推进订单状态
         */
        int affected =
                orderMapper.update(
                        null,
                        new LambdaUpdateWrapper<Order>()
                                .eq(
                                        Order::getOrderNo,
                                        orderNo
                                )
                                .eq(
                                        Order::getStatus,
                                        OrderStatus.PENDING_PAYMENT.getCode()
                                )
                                .set(
                                        Order::getStatus,
                                        OrderStatus.PENDING_SHIPMENT.getCode()
                                )
                                .set(
                                        Order::getPayTime,
                                        LocalDateTime.now()
                                )
                );

        if (affected == 0) {

            /*
             * 防并发重复通知：
             * 再查一次看看是不是另一个请求已经成功推进。
             */
            Order latest =
                    orderMapper.selectOne(
                            new LambdaQueryWrapper<Order>()
                                    .eq(
                                            Order::getOrderNo,
                                            orderNo
                                    )
                    );

            if (latest != null
                    && OrderStatus.PENDING_SHIPMENT
                    .getCode()
                    .equals(latest.getStatus())) {

                return Boolean.TRUE;
            }

            throw new BusinessException(
                    ErrorCode.ORDER_STATUS_INVALID
            );
        }

        return Boolean.TRUE;
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
                Result<InventoryReserveResult> result = inventoryClient.reserve(request);
                if (result == null || result.getData() == null || !Boolean.TRUE.equals(result.getData().getSuccess())) {
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
