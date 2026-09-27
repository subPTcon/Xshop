package org.michael.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDetailSkuVO {

    private Long id;

    private String specJson;

    private BigDecimal price;

    private Integer stock;
}
