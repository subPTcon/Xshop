package org.michael.logistics.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogisticsTrackVO {

    private String location;

    private String description;

    private LocalDateTime trackTime;


}
