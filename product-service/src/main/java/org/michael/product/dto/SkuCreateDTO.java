package org.michael.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuCreateDTO {

    @NotBlank(message = "SKU编码不能为空")
    @Size(max = 64, message = "SKU编码长度不能超过64个字符")
    private String skuCode;

    @Size(max = 255, message = "SKU规格内容不能超过255个字符")
    private String specJson;

    @NotNull(message = "SKU价格不能为空")
    @DecimalMin(value = "0.01", message = "SKU价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "SKU价格最多8位整数和2位小数")
    private BigDecimal price;

    @Size(max = 255, message = "SKU图片地址不能超过255个字符")
    private String image;
}
