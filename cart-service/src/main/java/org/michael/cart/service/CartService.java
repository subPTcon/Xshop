package org.michael.cart.service;

import org.michael.cart.dto.CartItemAddDTO;

public interface CartService {

    Boolean addItem(Long userId, CartItemAddDTO dto);
}
