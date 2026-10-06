package org.michael.payment.client;

import org.michael.common.result.Result;
import org.michael.payment.client.dto.OrderDTO;
import org.michael.payment.client.dto.OrderPaidRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/internal/orders/{orderNo}")
    Result<OrderDTO> getOrder(
            @PathVariable("orderNo") String orderNo
    );

    @PostMapping("/internal/orders/{orderNo}/paid")
    Result<Boolean> markPaid(
            @PathVariable("orderNo")
            String orderNo,

            @RequestBody
            OrderPaidRequest request
    );
}
