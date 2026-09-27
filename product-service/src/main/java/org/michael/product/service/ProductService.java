package org.michael.product.service;

import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.dto.ProductListQueryDTO;
import org.michael.product.vo.ProductDetailVO;
import org.michael.product.vo.ProductPageVO;
import org.michael.product.vo.ProductSkuVO;
import org.michael.product.vo.SkuDetailVO;

import java.util.List;

public interface ProductService {

    Long addProduct(ProductCreateDTO dto);

    SkuDetailVO getSkuById(Long skuId);

    List<ProductSkuVO> getProductSkus(Long productId);

    ProductDetailVO getProductDetail(Long productId);

    ProductPageVO getProductList(ProductListQueryDTO query);

    void updateProductStatus(Long id, Integer status);
}
