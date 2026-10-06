package org.michael.payment.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDetailVO {

    private String paymentNo;

    private String orderNo;

    private BigDecimal amount;

    private Integer status;

    private Integer payChannel;


}
