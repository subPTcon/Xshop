package org.michael.order.service;

import org.michael.order.dto.OrderCreateDTO;
import org.michael.order.vo.OrderCreateVO;

public interface OrderService {

    OrderCreateVO createOrder(Long userId, OrderCreateDTO dto);
}
