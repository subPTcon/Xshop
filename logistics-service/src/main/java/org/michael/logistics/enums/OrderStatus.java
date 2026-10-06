package org.michael.logistics.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING_SHIPMENT(2, "待发货"),
    SHIPPED(3, "已发货");

    private final Integer code;
    private final String description;
}
