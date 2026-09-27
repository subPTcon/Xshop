package org.michael.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.service.ProductService;
import org.michael.product.vo.ProductCreateVO;
import org.michael.common.result.Result;
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
}
