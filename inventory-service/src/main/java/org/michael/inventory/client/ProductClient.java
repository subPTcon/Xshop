package org.michael.inventory.client;

import org.michael.common.result.Result;
import org.michael.inventory.dto.SkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/skus/get/{skuId}")
    Result<SkuDTO> getSkuById(
            @PathVariable("skuId") Long skuId
    );
}
