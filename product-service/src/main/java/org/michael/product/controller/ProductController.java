package org.michael.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.product.dto.ProductCreateDTO;
import org.michael.product.service.ProductService;
import org.michael.product.vo.ProductCreateVO;
import org.michael.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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

}
