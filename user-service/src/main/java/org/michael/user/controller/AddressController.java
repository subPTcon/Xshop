package org.michael.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.user.common.context.UserContext;
import org.michael.user.dto.AddAddressRequest;
import org.michael.user.dto.AddAddressResponse;
import org.michael.user.dto.AddressResponse;
import org.michael.common.result.Result;
import org.michael.user.service.AddressService;
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

    @PutMapping("/update/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @Valid @RequestBody AddAddressRequest request) {
        log.info("POST /addresses/update/{id} id={}, 时间:{}", id, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        addressService.updateAddress(userId, id, request);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        log.info("DELETE /addresses/delete/{id} id={}, 时间:{}", id, LocalDateTime.now());
        Long userId = UserContext.getUserId();
        addressService.deleteAddress(userId, id);
        return Result.ok();
    }
}
