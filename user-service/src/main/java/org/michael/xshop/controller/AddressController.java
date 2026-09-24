package org.michael.xshop.controller;

import lombok.RequiredArgsConstructor;
import org.michael.xshop.common.context.UserContext;
import org.michael.xshop.common.result.Result;
import org.michael.xshop.dto.AddressResponse;
import org.michael.xshop.service.AddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<AddressResponse>> listAddresses() {
        Long userId = UserContext.getUserId();
        return Result.ok(addressService.listAddresses(userId));
    }
    
}
