package org.michael.cart.service;

import org.michael.cart.dto.CartItemAddDTO;
import org.michael.cart.dto.CartItemUpdateDTO;

public interface CartService {

    Boolean addItem(Long userId, CartItemAddDTO dto);

    Boolean updateItem(Long userId, Long skuId, CartItemUpdateDTO dto);
}
