package org.michael.product.service;

import org.michael.product.dto.ProductCreateDTO;

public interface ProductService {

    /**
     * 新增商品
     *
     * @param dto
     * @return 商品ID
     */
    Long addProduct(ProductCreateDTO dto);
}
