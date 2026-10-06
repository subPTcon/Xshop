package org.michael.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListItemVO {

    private String orderNo;

    private BigDecimal totalAmount;

    private Integer status;

    private LocalDateTime createTime;
}
