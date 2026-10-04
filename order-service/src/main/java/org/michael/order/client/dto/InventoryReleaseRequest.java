package org.michael.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryReleaseRequest {

    private Long skuId;

    private String orderNo;
}
