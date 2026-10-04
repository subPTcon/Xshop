package org.michael.order.client;

import org.michael.common.result.Result;
import org.michael.order.client.dto.CartRemoveItemsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cart-service")
public interface CartClient {

    @PostMapping("/cart/remove-items")
    Result<Boolean> removeItems(@RequestBody CartRemoveItemsRequest request);
}
