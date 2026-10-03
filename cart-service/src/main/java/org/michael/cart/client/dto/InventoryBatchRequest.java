package org.michael.cart.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InventoryBatchRequest {

    private List<Long> skuIds;
}
