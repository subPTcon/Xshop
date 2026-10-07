package org.michael.logistics.client;

import org.michael.common.result.Result;
import org.michael.logistics.client.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/internal/orders/{orderNo}/shipped")
    Result<Boolean> markShipped(@PathVariable("orderNo") String orderNo);

    @GetMapping("/internal/orders/{orderNo}")
    Result<OrderDTO> getOrder(@PathVariable("orderNo") String orderNo);
}
