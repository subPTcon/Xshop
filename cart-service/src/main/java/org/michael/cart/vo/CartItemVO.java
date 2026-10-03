package org.michael.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {

    private Long skuId;

    private Long productId;

    private String productTitle;

    private String specJson;

    private BigDecimal price;

    private String image;

    private Integer count;

    private Integer availableStock;

    private Integer skuStatus;

    private Integer productStatus;

    private Boolean available;
}
