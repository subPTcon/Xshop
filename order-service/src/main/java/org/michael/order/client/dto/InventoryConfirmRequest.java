package org.michael.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryConfirmRequest {

    private Long skuId;

    private String orderNo;
}
