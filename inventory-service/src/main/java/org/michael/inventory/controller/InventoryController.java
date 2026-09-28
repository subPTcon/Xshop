package org.michael.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.inventory.dto.InventoryInitDTO;
import org.michael.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
}
