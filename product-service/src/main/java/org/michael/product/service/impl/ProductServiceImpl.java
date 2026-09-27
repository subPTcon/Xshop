package org.michael.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.dto.ProductListQueryDTO;
import org.michael.product.dto.SkuCreateDTO;
import org.michael.product.mapper.CategoryMapper;
import org.michael.product.mapper.ProductMapper;
import org.michael.product.mapper.SkuMapper;
import org.michael.product.pojo.Product;
import org.michael.product.pojo.ProductMinPrice;
import org.michael.product.pojo.Sku;
import org.michael.product.service.ProductService;
import org.michael.product.vo.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public ProductPageVO getProductList(ProductListQueryDTO query) {
        // 1.构造商品查询条件
        LambdaQueryWrapper<Product> wrapper =
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(
                                query.getCategoryId() != null,
                                Product::getCategoryId,
                                query.getCategoryId()
                        )
                        .like(
                                query.getKeyword() != null
                                && !query.getKeyword().isBlank(),
                                Product::getTitle,
                                query.getKeyword()
                        )
                        .orderByDesc(Product::getId);

        // 2.分页查询商品
        Page<Product> page = new Page<>(
                query.getPage(),
                query.getSize()
        );
        Page<Product> productPage = productMapper.selectPage(page, wrapper);
        List<Product> products = productPage.getRecords();

        // 当前页无数据，直接返回
        if (products.isEmpty()) {
            return new ProductPageVO(
                    List.of(),
                    productPage.getTotal()
            );
        }

        // 3.拿出当前页所有 productId
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        // 4.一次性查当前页面商品的最低 SKU 价格
        List<ProductMinPrice> minPrices = skuMapper.selectMinPriceByProductIds(productIds);

        // 5.转成Map，方便匹配
        Map<Long, BigDecimal> priceMap =
                minPrices.stream()
                        .collect(Collectors.toMap(
                                ProductMinPrice::getProductId,
                                ProductMinPrice::getMinPrice
                        ));

        // 6.转VO
        List<ProductListItemVO> list =
                products.stream()
                        .map(product -> {
                            ProductListItemVO vo = new ProductListItemVO();
                            vo.setId(product.getId());
                            vo.setTitle(product.getTitle());
                            vo.setMainImage(product.getMainImage());
                            vo.setMinPrice(priceMap.get(product.getId()));

                            return vo;
                        }).toList();

        return new ProductPageVO(list, productPage.getTotal());
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
