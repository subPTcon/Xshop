package org.michael.product.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductMinPrice {

    private Long productId;

    private BigDecimal minPrice;
}
