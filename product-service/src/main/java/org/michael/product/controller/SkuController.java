package org.michael.product.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.product.service.ProductService;
import org.michael.product.vo.SkuDetailVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/skus")
public class SkuController {

    private final ProductService productService;

    @GetMapping("/get/{skuId}")
    public Result<SkuDetailVO> getSkuById(@PathVariable @Min(value = 1, message = "SKU ID必须大于0") Long skuId) {
        log.info("GET /skus/get/{skuId} skuId={}, 时间:{}", skuId, LocalDateTime.now());
        SkuDetailVO sku = productService.getSkuById(skuId);
        return Result.ok(sku);
    }
}
