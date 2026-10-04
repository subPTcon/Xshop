package org.michael.order.service;

import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.vo.OrderCreateVO;
import org.michael.order.vo.OrderTokenVO;

public interface OrderService {

    OrderCreateVO createOrder(Long userId, OrderCreateDTO dto);

    OrderTokenVO generateOrderToken(Long userId);
}
