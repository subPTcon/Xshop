package org.michael.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderCancelDTO {

    @NotBlank(message = "取消原因不能为空")
    @Size(max = 128, message = "取消原因不能超过128个字符")
    private String reason;
}
