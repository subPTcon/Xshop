package org.michael.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.dto.SkuCreateDTO;
import org.michael.product.mapper.ProductMapper;
import org.michael.product.mapper.SkuMapper;
import org.michael.product.pojo.Product;
import org.michael.product.pojo.Sku;
import org.michael.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addProduct(ProductCreateDTO dto) {
        Product product = new Product();

        product.setCategoryId(dto.getCategoryId());
        product.setTitle(dto.getTitle());
        product.setDetailHtml(dto.getDetailHtml());
        product.setStatus(1);
        product.setSaleCount(0);
        productMapper.insert(product);

        // MyBatis-Plus 插入后会自动回填主键
        Long productId = product.getId();

        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            for (SkuCreateDTO skuDTO: dto.getSkus()) {
                Sku sku = new Sku();

                sku.setProductId(productId);
                sku.setSkuCode(skuDTO.getSkuCode());
                sku.setSpecJson(skuDTO.getSpecJson());
                sku.setPrice(skuDTO.getPrice());
                sku.setImage(skuDTO.getImage());

                sku.setStatus(1);

                skuMapper.insert(sku);
            }
        }

        return productId;
    }
}
