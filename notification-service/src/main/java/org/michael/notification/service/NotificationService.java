package org.michael.notification.service;

import org.michael.notification.vo.NotificationPageVO;

public interface NotificationService {

    NotificationPageVO getNotifications(Long userId, Integer page, Integer size, Integer isRead);

    Long getUnreadCount(Long userId);

    Boolean markAsRead(Long userId, Long notificationId);
}
