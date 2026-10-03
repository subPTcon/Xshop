package org.michael.inventory.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.inventory.dto.*;
import org.michael.inventory.service.InventoryService;
import org.michael.inventory.vo.InventoryReserveVO;
import org.michael.inventory.vo.InventoryStockVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/init")
    public Result<Boolean> initInventory(
            @Valid @RequestBody InventoryInitDTO dto
    ) {
        log.info("POST /inventory/init dto={}, 时间: {}", dto, LocalDateTime.now());
        inventoryService.initInventory(dto);
        return Result.ok(true);
    }

    @GetMapping("/get/{skuId}")
    public Result<InventoryStockVO> getAvailableStock(
            @PathVariable
            @Min(value = 1, message = "SKU ID必须大于0")
            Long skuId
    ) {
        log.info("GET /inventory/get/{skuId} skuId={}, 时间:{}", skuId, LocalDateTime.now());
        return Result.ok(inventoryService.getAvailableStock(skuId));
    }

    @PostMapping("/add")
    public Result<Boolean> addStock(
            @Valid @RequestBody InventoryAddDTO dto
    ) {
        log.info("POST /inventory/add dto={}, 时间:{}", dto, LocalDateTime.now());
        inventoryService.addStock(dto);
        return Result.ok(true);
    }

    @PostMapping("/reserve")
    public Result<InventoryReserveVO> reserve(
            @Valid @RequestBody ReserveInventoryDTO dto
    ) {
        log.info("POST /inventory/reserve dto={}, 时间:{}", dto, LocalDateTime.now());
        return Result.ok(inventoryService.reserve(dto));
    }

    @PostMapping("/release")
    public Result<Boolean> release(
            @Valid
            @RequestBody InventoryReleaseDTO dto
    ) {
        log.info("POST /inventory/release dto={}, 时间:{}", dto, LocalDateTime.now());
        return Result.ok(inventoryService.release(dto));
    }

    @PostMapping("/confirm")
    public Result<Boolean> confirm(
            @Valid
            @RequestBody InventoryConfirmDTO dto
    ) {
        log.info("POST /inventory/confirm dto={}, 时间:{}", dto, LocalDateTime.now());
        return Result.ok(inventoryService.confirm(dto));
    }

    @PostMapping("/batch")
    public Result<List<InventoryStockVO>> batchGetAvailableStock(
            @Valid
            @RequestBody
            InventoryBatchDTO dto
    ) {
        log.info("POST /inventory/batch dto={}, 时间:{}", dto, LocalDateTime.now());
        return Result.ok(inventoryService.batchGetAvailableStock(dto));
    }
}
