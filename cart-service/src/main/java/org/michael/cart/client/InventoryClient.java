package org.michael.cart.client;

import org.michael.cart.client.dto.InventoryBatchRequest;
import org.michael.cart.client.dto.InventoryStockDTO;
import org.michael.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/inventory/batch")
    Result<List<InventoryStockDTO>> batchGetStock(@RequestBody InventoryBatchRequest request);
}
