package org.michael.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogisticsShipDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "承运商不能为空")
    private String carrier;

    @NotBlank(message = "当前位置不能为空")
    private String location;

    @NotBlank(message = "物流描述不能为空")
    private String description;
}
