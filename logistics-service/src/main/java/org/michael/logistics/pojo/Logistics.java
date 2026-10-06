package org.michael.logistics.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_logistics")
@Data
public class Logistics {

    private Long id;

    private String logisticsNo;

    private String orderNo;

    private String carrier;

    private Integer status;

    private String currentLocation;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
