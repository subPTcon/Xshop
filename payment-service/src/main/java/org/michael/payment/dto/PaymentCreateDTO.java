package org.michael.payment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCreateDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 1 模拟支付宝
     * 2 模拟微信
     */
    @NotNull(message = "支付渠道不能为空")
    @Min(value = 1, message = "支付渠道不正确")
    @Max(value = 2, message = "支付渠道不正确")
    private Integer payChannel;
}
