package org.michael.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LogisticsUpdateDTO {

    @NotNull(message = "物流状态不能为空")
    private Integer status;

    @NotBlank(message = "当前位置不能为空")
    private String location;

    @NotBlank(message = "物流描述不能为空")
    private String description;
}
