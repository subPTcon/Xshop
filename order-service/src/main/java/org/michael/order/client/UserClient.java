package org.michael.order.client;

import org.michael.common.result.Result;
import org.michael.order.client.dto.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/internal/users/{userId}/addresses/{addressId}")
    Result<AddressDTO> getAddress(
            @PathVariable("userId") Long userId,
            @PathVariable("addressId") Long addressId
    );
}
