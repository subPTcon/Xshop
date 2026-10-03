package org.michael.order.client;

import org.michael.common.result.Result;
import org.michael.order.client.dto.InventoryReserveRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping
    Result<Boolean> reserve(@RequestBody InventoryReserveRequest request);
}
