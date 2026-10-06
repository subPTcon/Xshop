package org.michael.logistics.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogisticsStatus {

    PENDING_SHIPMENT(0, "待发货"),
    SHIPPED(1, "已发货"),
    IN_TRANSIT(2, "运输中"),
    DELIVERED(3, "已签收");

    private final Integer code;

    private final String description;
}
