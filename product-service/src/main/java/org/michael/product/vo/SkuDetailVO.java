package org.michael.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuDetailVO {

    private Long skuId;

    private Long productId;

    private BigDecimal price;

    private Integer status;
}
