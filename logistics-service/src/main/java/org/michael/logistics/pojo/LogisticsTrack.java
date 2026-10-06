package org.michael.logistics.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_logistics_track")
public class LogisticsTrack {

    private Long id;

    private String logisticsNo;

    private String location;

    private String description;

    private LocalDateTime trackTime;
}
