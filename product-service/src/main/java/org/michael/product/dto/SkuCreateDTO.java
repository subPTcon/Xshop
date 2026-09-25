package org.michael.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuCreateDTO {

    private String skuCode;

    private String specJson;

    private BigDecimal price;

    private String image;
}
