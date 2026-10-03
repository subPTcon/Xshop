package org.michael.cart.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SkuBatchRequest {

    private List<Long> skuIds;
}
