package org.michael.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderCreateVO {

    private String orderNo;

    private BigDecimal totalAmount;
}
