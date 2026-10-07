package org.michael.logistics.vo;

import lombok.Data;

import java.util.List;

@Data
public class LogisticsDetailVO {

    private String logisticsNo;

    private String orderNo;

    private String carrier;

    private Integer status;

    private String currentLocation;

    private List<LogisticsTrackVO> tracks;
}
