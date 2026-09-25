package org.michael.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductCreateDTO {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 128, message = "商品标题长度不能超过128个字符")
    private String title;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @Size(max = 65535, message = "商品的详情内容过长")
    private String detailHtml;

    @Valid
    @NotEmpty(message = "商品至少需要一个SKU")
    private List<SkuCreateDTO> skus;
}
