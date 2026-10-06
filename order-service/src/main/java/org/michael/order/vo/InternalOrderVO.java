package org.michael.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InternalOrderVO {

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private Integer status;


}
