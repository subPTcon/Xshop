package org.michael.cart.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.cart.dto.CartItemAddDTO;
import org.michael.cart.dto.CartItemUpdateDTO;
import org.michael.cart.service.CartService;
import org.michael.common.context.UserContext;
import org.michael.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public Result<Boolean> addItem(@Valid @RequestBody CartItemAddDTO dto) {
        log.info("POST /cart/items dto={}, 时间:{}", dto, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(cartService.addItem(userId, dto));
    }

    @PutMapping("/items/{skuId}")
    public Result<Boolean> updateItem(
            @PathVariable
            @Min(value = 1, message = "SKU ID必须大于0")
            Long skuId,

            @Valid
            @RequestBody
            CartItemUpdateDTO dto
    ) {
        log.info("PUT /cart/items/{skuId} skuId={}, dto={}, 时间:{}", skuId, dto, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(cartService.updateItem(userId, skuId, dto));
    }
}
