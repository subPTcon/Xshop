package org.michael.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusUpdateDTO {

    @NotNull(message = "商品状态不能为空")
    @Min(value = 0, message = "商品状态只能为0或1")
    @Max(value = 1, message = "商品状态只能为0或1")
    private Integer status;
}
