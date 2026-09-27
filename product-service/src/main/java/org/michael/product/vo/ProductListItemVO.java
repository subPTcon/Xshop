package org.michael.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListItemVO {

    private Long id;

    private String title;

    private String mainImage;

    private BigDecimal minPrice;
}
