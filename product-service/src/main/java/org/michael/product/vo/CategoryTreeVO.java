package org.michael.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryTreeVO {

    private Long id;

    private String name;

    private List<CategoryTreeVO> children;
}
