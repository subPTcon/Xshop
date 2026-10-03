package org.michael.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SkuBatchQueryDTO {

    @NotEmpty(message = "SKU ID列表不能为空")
    @Size(max = 100, message = "一次性最多查询100个SKU")
    private List<
            @NotNull(message = "SKU ID不能为空")
            @Min(value = 1, message = "SKU ID必须大于0")
            Long> skuIds;
}
