package org.michael.notification.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;

    private Integer type;

    private String bizType;

    private String bizNo;

    private String title;

    private String content;

    private Boolean read;

    private LocalDateTime createTime;

    private LocalDateTime readTime;
}
