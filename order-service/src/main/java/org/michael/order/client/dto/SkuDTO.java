package org.michael.order.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuDTO {

    private Long skuId;

    private Long productId;

    private String productTitle;

    private String specJson;

    private BigDecimal price;

    private String image;

    private Integer skuStatus;

    private Integer productStatus;
}
