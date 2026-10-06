package org.michael.order.service;

import org.michael.order.dto.OrderCancelDTO;
import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.vo.OrderCreateVO;
import org.michael.order.vo.OrderDetailVO;
import org.michael.order.vo.OrderPageVO;
import org.michael.order.vo.OrderTokenVO;

public interface OrderService {

    OrderCreateVO createOrder(Long userId, OrderCreateDTO dto);

    OrderTokenVO generateOrderToken(Long userId);

    OrderDetailVO getOrderDetail(Long userId, String orderNo);

    OrderPageVO getOrderList(Long userId, Integer status, Integer page, Integer size);

    Boolean cancelOrder(Long userId, String orderNo, OrderCancelDTO dto);

    Boolean confirmOrder(Long userId, String orderNo);
}
