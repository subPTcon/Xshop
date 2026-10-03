package org.michael.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class CartRemoveItemsDTO {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @NotEmpty(message = "SKU ID列表不能为空")
    private List<
            @NotNull(message = "SKU ID不能为空")
            @Min(value = 1, message = "SKU ID必须大于0")
            Long> skuIds;
}
