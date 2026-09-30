package org.michael.inventory.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryReserveVO {

    private Boolean success;

    private String reason;
}
