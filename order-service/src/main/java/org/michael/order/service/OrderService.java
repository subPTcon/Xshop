package org.michael.order.service;

import org.michael.order.dto.OrderCancelDTO;
import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.vo.*;

import java.time.LocalDateTime;

public interface OrderService {

    OrderCreateVO createOrder(Long userId, OrderCreateDTO dto);

    OrderTokenVO generateOrderToken(Long userId);

    OrderDetailVO getOrderDetail(Long userId, String orderNo);

    OrderPageVO getOrderList(Long userId, Integer status, Integer page, Integer size);

    Boolean cancelOrder(Long userId, String orderNo, OrderCancelDTO dto);

    Boolean confirmOrder(Long userId, String orderNo);

    InternalOrderVO getInternalOrder(String orderNo);

    Boolean markPaid(String orderNo, LocalDateTime payTime);

    Boolean markShipped(String orderNo);
}
