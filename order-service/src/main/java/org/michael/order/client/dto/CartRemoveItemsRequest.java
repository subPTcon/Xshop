package org.michael.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartRemoveItemsRequest {

    private Long userId;

    private List<Long> skuIds;
}
