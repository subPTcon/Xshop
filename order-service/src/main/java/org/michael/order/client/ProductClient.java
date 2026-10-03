package org.michael.order.client;

import org.michael.common.result.Result;
import org.michael.order.client.dto.SkuBatchRequest;
import org.michael.order.client.dto.SkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {

    @PostMapping("/skus/batch")
    Result<List<SkuDTO>> batchGetSkus(@RequestBody SkuBatchRequest request);
}
