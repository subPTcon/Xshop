package org.michael.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull(message = "收货地址不能为空")
    @Min(value = 1, message = "地址ID必须大于0")
    private Long addressId;

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemCreateDTO> items;

    @NotNull(message = "防重Token不能为空")
    private String idempotentToken;
}
