package org.michael.inventory.service;

import org.michael.inventory.dto.*;
import org.michael.inventory.vo.InventoryReserveVO;
import org.michael.inventory.vo.InventoryStockVO;

public interface InventoryService {

    void initInventory(InventoryInitDTO dto);

    InventoryStockVO getAvailableStock(Long skuId);

    void addStock(InventoryAddDTO dto);

    InventoryReserveVO reserve(ReserveInventoryDTO dto);

    Boolean release(InventoryReleaseDTO dto);

    Boolean confirm(InventoryConfirmDTO dto);
}
