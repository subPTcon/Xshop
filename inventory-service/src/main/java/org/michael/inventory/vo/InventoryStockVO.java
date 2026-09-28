package org.michael.inventory.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryStockVO {

    private Long skuId;

    private Integer availableStock;
}
