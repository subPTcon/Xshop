package org.michael.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuBatchVO {

    private Long skuId;

    private Long productId;

    private String productTitle;

    private String specJson;

    private BigDecimal price;

    private String image;

    private Integer status;
}
