package org.michael.order.service;

import org.michael.order.pojo.Order;
import org.michael.order.pojo.OrderItem;

import java.util.List;

public interface OrderPersistenceService {

    void saveOrder(Order order, List<OrderItem> items);
}
