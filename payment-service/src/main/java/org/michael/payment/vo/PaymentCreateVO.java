package org.michael.payment.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentCreateVO {

    private String paymentNo;

    private String payUrl;


}
