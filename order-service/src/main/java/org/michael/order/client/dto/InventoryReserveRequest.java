package org.michael.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryReserveRequest {

    private Long skuId;

    private String orderNo;

    private Integer count;
}
