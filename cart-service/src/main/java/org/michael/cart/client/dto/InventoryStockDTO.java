package org.michael.cart.client.dto;

import lombok.Data;

@Data
public class InventoryStockDTO {

    private Long skuId;

    private Integer availableStock;
}
