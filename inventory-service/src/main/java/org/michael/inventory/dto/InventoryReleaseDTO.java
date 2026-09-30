package org.michael.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryReleaseDTO {

    @NotNull(message = "SKU ID不能为空")
    @Min(value = 1, message = "SKU ID必须大于0")
    private Long skuId;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
