package org.michael.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentCallbackDTO {

    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;

    /**
     * 1 支付成功
     * 2 支付失败
     */
    @NotNull(message = "支付状态不能为空")
    private Integer status;

    @NotNull
    private BigDecimal amount;

    private LocalDateTime payTime;

    @NotBlank(message = "签名不能为空")
    private String sign;
}
