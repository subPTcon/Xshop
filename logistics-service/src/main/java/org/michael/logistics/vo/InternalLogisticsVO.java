package org.michael.logistics.vo;

import lombok.Data;

@Data
public class InternalLogisticsVO {

    private String logisticsNo;

    private String orderNo;

    private String carrier;

    private Integer status;

    private String currentLocation;
}
