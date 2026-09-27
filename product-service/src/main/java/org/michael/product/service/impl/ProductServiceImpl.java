package org.michael.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.dto.SkuCreateDTO;
import org.michael.product.mapper.CategoryMapper;
import org.michael.product.mapper.ProductMapper;
import org.michael.product.mapper.SkuMapper;
import org.michael.product.pojo.Product;
import org.michael.product.pojo.Sku;
import org.michael.product.service.ProductService;
import org.michael.product.vo.ProductDetailSkuVO;
import org.michael.product.vo.ProductDetailVO;
import org.michael.product.vo.ProductSkuVO;
import org.michael.product.vo.SkuDetailVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addProduct(ProductCreateDTO dto) {
        validateProductCreate(dto);

        Product product = new Product();

        product.setCategoryId(dto.getCategoryId());
        product.setTitle(dto.getTitle());
        product.setDetailHtml(dto.getDetailHtml());
        product.setStatus(1);
        product.setSaleCount(0);

        productMapper.insert(product);

        Long productId = product.getId();

        for (SkuCreateDTO skuDTO : dto.getSkus()) {

            Sku sku = new Sku();

            sku.setProductId(productId);
            sku.setSkuCode(skuDTO.getSkuCode());
            sku.setSpecJson(skuDTO.getSpecJson());
            sku.setPrice(skuDTO.getPrice());
            sku.setImage(skuDTO.getImage());
            sku.setStatus(1);

            try {
                skuMapper.insert(sku);
            } catch (DuplicateKeyException e) {
                throw new BusinessException(ErrorCode.SKU_CODE_EXISTS);
            }
        }

        return productId;
    }

    @Override
    public SkuDetailVO getSkuById(Long skuId) {
        Sku sku = skuMapper.selectById(skuId);

        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        SkuDetailVO vo = new SkuDetailVO();

        vo.setSkuId(sku.getId());
        vo.setProductId(sku.getProductId());
        vo.setPrice(sku.getPrice());
        vo.setStatus(sku.getStatus());

        return vo;
    }

    @Override
    public List<ProductSkuVO> getProductSkus(Long productId) {
        // 1.先校验商品是否存在
        Product product = productMapper.selectById(productId);

        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 2.查询该商品下所有 SKU
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getProductId, productId)
                        .orderByAsc(Sku::getId)
        );

        return skus.stream()
                .map(sku -> {
                    ProductSkuVO vo = new ProductSkuVO();

                    vo.setSkuId(sku.getId());
                    vo.setPrice(sku.getPrice());
                    vo.setSpecJson(sku.getSpecJson());

                    return vo;
                }).toList();
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        // 1.查询商品
        Product product = productMapper.selectById(productId);

        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 2.查询商品下所有 SKU
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getProductId, productId)
                        .orderByAsc(Sku::getId)
        );

        // 3.转换SKU
        List<ProductDetailSkuVO> skuVOList = skus.stream()
                .map(sku -> {
                    ProductDetailSkuVO vo = new ProductDetailSkuVO();

                    vo.setId(sku.getId());
                    vo.setSpecJson(sku.getSpecJson());
                    vo.setPrice(sku.getPrice());

                    vo.setStock(null);

                    return vo;
                }).toList();

        // 4.组装商品详情
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setTitle(product.getTitle());
        vo.setDetailHtml(product.getDetailHtml());
        vo.setSkus(skuVOList);

        return vo;
    }

    private void validateProductCreate(ProductCreateDTO dto) {

        // 分类是否存在
        if (categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        List<String> skuCodes = dto.getSkus()
                .stream()
                .map(SkuCreateDTO::getSkuCode)
                .toList();

        // 请求内部是否重复
        if (new HashSet<>(skuCodes).size() != skuCodes.size()) {
            throw new BusinessException(ErrorCode.SKU_CODE_EXISTS);
        }

        // 数据库是否已经存在
        Long count = skuMapper.selectCount(
                new LambdaQueryWrapper<Sku>()
                        .in(Sku::getSkuCode, skuCodes)
        );

        if (count > 0) {
            throw new BusinessException(ErrorCode.SKU_CODE_EXISTS);
        }
    }
}
