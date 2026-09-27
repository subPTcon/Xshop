package org.michael.product.service;

import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.vo.ProductSkuVO;
import org.michael.product.vo.SkuDetailVO;

import java.util.List;

public interface ProductService {

    /**
     * 新增商品
     *
     * @param dto
     * @return 商品ID
     */
    Long addProduct(ProductCreateDTO dto);

    SkuDetailVO getSkuById(Long skuId);

    List<ProductSkuVO> getProductSkus(Long productId);
}
