package org.michael.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryInitDTO {

    @NotNull(message = "SKU ID不能为空")
    @Min(value = 1, message = "SKU ID必须大于0")
    private Long skuId;

    @NotNull(message = "初始化库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;
}
