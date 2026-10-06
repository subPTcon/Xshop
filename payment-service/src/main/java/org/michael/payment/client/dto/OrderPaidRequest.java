package org.michael.payment.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderPaidRequest {

    private LocalDateTime payTime;
}
