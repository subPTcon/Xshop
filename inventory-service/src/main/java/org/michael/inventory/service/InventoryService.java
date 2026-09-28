package org.michael.inventory.service;

import org.michael.inventory.dto.InventoryAddDTO;
import org.michael.inventory.dto.InventoryInitDTO;
import org.michael.inventory.vo.InventoryStockVO;

public interface InventoryService {

    void initInventory(InventoryInitDTO dto);

    InventoryStockVO getAvailableStock(Long skuId);

    void addStock(InventoryAddDTO dto);
}
