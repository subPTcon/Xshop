package org.michael.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProductDetailVO {

    private Long id;

    private String title;

    private String detailHtml;

    private List<ProductDetailSkuVO> skus;
}
