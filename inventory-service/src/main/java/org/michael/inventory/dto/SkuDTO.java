package org.michael.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuDTO {

    private Long skuId;

    private Long productId;

    private BigDecimal price;

    private Integer status;
}
