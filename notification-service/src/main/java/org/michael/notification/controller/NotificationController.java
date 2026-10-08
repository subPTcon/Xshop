package org.michael.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.context.UserContext;
import org.michael.common.result.Result;
import org.michael.notification.service.NotificationService;
import org.michael.notification.vo.NotificationPageVO;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<NotificationPageVO> list(
            @RequestParam(defaultValue = "1")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size,

            @RequestParam(required = false)
            Integer isRead
    ) {
        log.info("GET /notifications page={}, size={}, isRead={}", page, size, isRead);
        Long userId = UserContext.getUserId();
        return Result.ok(notificationService.getNotifications(userId, page, size, isRead));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        log.info("GET /notifications/unread-count");
        Long userId = UserContext.getUserId();
        return Result.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        log.info("PUT /notifications/{id}/read id={}", id);
        Long userId = UserContext.getUserId();
        return Result.ok(notificationService.markAsRead(userId, id));
    }
}
