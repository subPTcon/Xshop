package org.michael.payment.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDTO {

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private Integer status;
}
