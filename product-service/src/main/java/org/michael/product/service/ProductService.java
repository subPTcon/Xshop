package org.michael.product.service;

import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.vo.SkuDetailVO;

public interface ProductService {

    /**
     * 新增商品
     *
     * @param dto
     * @return 商品ID
     */
    Long addProduct(ProductCreateDTO dto);

    SkuDetailVO getSkuById(Long skuId);
}
