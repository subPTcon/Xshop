package org.michael.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSkuVO {

    private Long skuId;

    private BigDecimal price;

    private String specJson;
}
