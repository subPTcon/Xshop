package org.michael.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private String eventId;

    private String orderNo;

    private Long userId;

    private String reason;


}
