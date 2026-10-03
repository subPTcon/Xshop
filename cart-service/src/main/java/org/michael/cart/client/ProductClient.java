package org.michael.cart.client;

import org.michael.cart.client.dto.SkuBatchDTO;
import org.michael.cart.client.dto.SkuBatchRequest;
import org.michael.cart.client.dto.SkuDTO;
import org.michael.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = "http://192.168.2.103:8082")
public interface ProductClient {

    @GetMapping("/skus/get/{skuId}")
    Result<SkuDTO> getSkuById(@PathVariable("skuId") Long skuId);

    @PostMapping("/skus/batch")
    Result<List<SkuBatchDTO>> batchGetSkus(@RequestBody SkuBatchRequest request);
}
