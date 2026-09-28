package org.michael.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAddDTO {

    @NotNull(message = "SKU ID不能为空")
    @Min(value = 1, message = "SKU ID必须大于0")
    private Long skuId;

    @NotNull(message = "补货数量不能为空")
    @Min(value = 1, message = "补货数量必须大于0")
    private Integer count;
}
