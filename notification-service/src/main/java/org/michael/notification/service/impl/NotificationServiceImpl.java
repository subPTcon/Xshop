package org.michael.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.notification.mapper.NotificationMapper;
import org.michael.notification.pojo.Notification;
import org.michael.notification.service.NotificationService;
import org.michael.notification.vo.NotificationPageVO;
import org.michael.notification.vo.NotificationVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public NotificationPageVO getNotifications(Long userId, Integer page, Integer size, Integer isRead) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(
                        Notification::getUserId,
                        userId
                );
        if (isRead != null) {
            wrapper.eq(
                    Notification::getIsRead,
                    isRead
            );
        }

        wrapper.orderByDesc(Notification::getCreateTime);

        Page<Notification> result = notificationMapper.selectPage(
                new Page<>(page, size),
                wrapper
        );

        List<NotificationVO> list = result.getRecords()
                .stream()
                .map(notification -> {
                    NotificationVO vo = new NotificationVO();
                    vo.setId(notification.getId());
                    vo.setType(notification.getType());
                    vo.setBizType(notification.getBizType());
                    vo.setBizNo(notification.getBizNo());
                    vo.setTitle(notification.getTitle());
                    vo.setContent(notification.getContent());
                    vo.setRead(Integer.valueOf(1).equals(notification.getIsRead()));
                    vo.setCreateTime(notification.getCreateTime());
                    vo.setReadTime(notification.getReadTime());

                    return vo;
                }).toList();
        return new NotificationPageVO(list, result.getTotal());
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );
    }

    @Override
    public Boolean markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectOne(
            new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getId, notificationId)
                    .eq(Notification::getUserId, userId)
        );
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        // 已经读过了，直接返回，保证幂等
        if (Integer.valueOf(1).equals(notification.getIsRead())) {
            return Boolean.TRUE;
        }

        int affected = notificationMapper.update(
                null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1)
                        .set(Notification::getReadTime, LocalDateTime.now())
        );

        return affected > 0;
    }
}
