package org.michael.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductPageVO {

    private List<ProductListItemVO> list;

    private Long total;
}
