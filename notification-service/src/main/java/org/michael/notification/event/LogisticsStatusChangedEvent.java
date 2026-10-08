package org.michael.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsStatusChangedEvent {

    private String eventId;

    private String logisticsNo;

    private String orderNo;

    private Long userId;

    private Integer status;

    private String location;

    private String description;


}
