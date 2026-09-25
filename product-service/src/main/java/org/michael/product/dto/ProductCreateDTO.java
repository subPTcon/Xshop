package org.michael.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductCreateDTO {

    private String title;

    private Long categoryId;

    private String detailHtml;

    private List<SkuCreateDTO> skus;
}
