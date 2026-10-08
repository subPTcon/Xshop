package org.michael.notification.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NotificationPageVO {

    private List<NotificationVO> list;

    private Long total;
}
