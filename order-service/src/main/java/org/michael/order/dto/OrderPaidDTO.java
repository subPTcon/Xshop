package org.michael.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderPaidDTO {

    private LocalDateTime payTime;
}
