package org.michael.xshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.xshop.common.context.UserContext;
import org.michael.xshop.common.result.Result;
import org.michael.xshop.dto.AddAddressRequest;
import org.michael.xshop.dto.AddAddressResponse;
import org.michael.xshop.dto.AddressResponse;
import org.michael.xshop.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/list")
    public Result<List<AddressResponse>> listAddresses() {
        log.info("GET /addresses/list 时间:{}", LocalDateTime.now());
        Long userId = UserContext.getUserId();
        return Result.ok(addressService.listAddresses(userId));
    }

    @PostMapping("/add")
    public Result<AddAddressResponse> addAddress(@Valid @RequestBody AddAddressRequest request) {
        log.info("POST /addresses/add 时间:{}", LocalDateTime.now());
        Long userId = UserContext.getUserId();
        Long addressId = addressService.addAddress(userId, request);
        return Result.ok(new AddAddressResponse(addressId));
    }

}
