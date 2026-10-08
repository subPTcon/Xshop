package org.michael.notification.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_notification")
public class Notification {

    private Long id;

    private String eventId;

    private Long userId;

    private Integer type;

    private String bizType;

    private String bizNo;

    private String title;

    private String content;

    private Integer isRead;

    private Integer sendStatus;

    private Integer retryCount;

    private LocalDateTime createTime;

    private LocalDateTime readTime;
}
