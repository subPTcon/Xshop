package org.michael.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategorySaveDTO {

    @NotNull(message = "父类目ID不能为空")
    @Min(value = 0, message = "父类目ID不能小于0")
    private Long parentId;

    @NotBlank
    @Size(max = 64, message = "类目名称长度不能超过64个字符")
    private String name;

    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}
