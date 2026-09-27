package org.michael.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.dto.ProductListQueryDTO;
import org.michael.product.dto.ProductStatusUpdateDTO;
import org.michael.product.service.ProductService;
import org.michael.product.vo.ProductCreateVO;
import org.michael.common.result.Result;
import org.michael.product.vo.ProductDetailVO;
import org.michael.product.vo.ProductPageVO;
import org.michael.product.vo.ProductSkuVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/add")
    public Result<ProductCreateVO> addProduct(@Valid @RequestBody ProductCreateDTO dto) {
        log.info("POST /products/add dto={}, 时间:{}", dto, LocalDateTime.now());
        Long productId = productService.addProduct(dto);
        return org.michael.common.result.Result.ok(new ProductCreateVO(productId));
    }

    @GetMapping("/{id}/skus")
    public Result<List<ProductSkuVO>> getProductSkus(@PathVariable @Min(value = 1, message = "商品ID必须大于0") Long id) {
        log.info("GET /product/{id}/skus id={}, 时间:{}", id, LocalDateTime.now());
        List<ProductSkuVO> skus = productService.getProductSkus(id);
        return Result.ok(skus);
    }

    @GetMapping("/get/{id}")
    public Result<ProductDetailVO> getProductDetail(
            @PathVariable
            @Min(value = 1, message = "商品ID必须大于0")
            Long id
    ) {
        log.info("GET /products/get/{id} id={}, 时间:{}", id, LocalDateTime.now());
        ProductDetailVO detail = productService.getProductDetail(id);
        return Result.ok(detail);
    }

    @GetMapping("/get")
    public Result<ProductPageVO> getProductList(@Valid ProductListQueryDTO query) {
        log.info("GET /products/get query={}, 时间:{}", query, LocalDateTime.now());
        ProductPageVO result = productService.getProductList(query);
        return Result.ok(result);
    }

    @PutMapping("/updateStatus/{id}")
    public Result<Boolean> updateProductStatus(
            @PathVariable @Min(value = 1, message = "商品ID必须大于0")
            Long id,
            @Valid @RequestBody
            ProductStatusUpdateDTO dto
    ) {
        log.info("/PUT /products/updateStatus/{id}, id={}, dto={}, 时间:{}", id, dto, LocalDateTime.now());
        productService.updateProductStatus(id, dto.getStatus());
        return Result.ok(true);
    }

}
