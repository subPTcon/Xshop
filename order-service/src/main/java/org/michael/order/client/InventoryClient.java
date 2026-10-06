package org.michael.order.client;

import jakarta.validation.Valid;
import org.michael.common.result.Result;
import org.michael.order.client.dto.InventoryConfirmRequest;
import org.michael.order.client.dto.InventoryReleaseRequest;
import org.michael.order.client.dto.InventoryReserveRequest;
import org.michael.order.client.dto.InventoryReserveResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/inventory/reserve")
    Result<InventoryReserveResult> reserve(@RequestBody InventoryReserveRequest request);

    @PostMapping("/inventory/release")
    Result<Boolean> release(@Valid @RequestBody InventoryReleaseRequest request);

    @PostMapping("/inventory/confirm")
    Result<Boolean> confirm(@Valid @RequestBody InventoryConfirmRequest request);
}
