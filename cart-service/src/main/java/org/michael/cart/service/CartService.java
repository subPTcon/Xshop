package org.michael.cart.service;

import org.michael.cart.dto.CartItemAddDTO;
import org.michael.cart.dto.CartItemUpdateDTO;
import org.michael.cart.vo.CartItemVO;

import java.util.List;

public interface CartService {

    Boolean addItem(Long userId, CartItemAddDTO dto);

    Boolean updateItem(Long userId, Long skuId, CartItemUpdateDTO dto);

    Boolean deleteItem(Long userId, Long skuId);

    List<CartItemVO> getCart(Long userId);

    Boolean clearCart(Long userId);
}
