package org.michael.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.michael.order.mapper.OrderItemMapper;
import org.michael.order.mapper.OrderMapper;
import org.michael.order.pojo.Order;
import org.michael.order.pojo.OrderItem;
import org.michael.order.service.OrderPersistenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderPersistenceServiceImpl implements OrderPersistenceService {

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(Order order, List<OrderItem> items) {
        orderMapper.insert(order);
        for (OrderItem item: items) {
            orderItemMapper.insert(item);
        }
    }
}
