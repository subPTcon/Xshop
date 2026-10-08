package org.michael.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {

    private String eventId;

    private String paymentNo;

    private String orderNo;

    private Long userId;

    private BigDecimal amount;
}
