package org.michael.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long skuId;

    private String productTitle;

    private String skuSpec;

    private BigDecimal price;

    private Integer count;
}
