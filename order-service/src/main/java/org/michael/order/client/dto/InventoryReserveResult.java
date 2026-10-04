package org.michael.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryReserveResult {

    private Boolean success;

    private String reason;
}
