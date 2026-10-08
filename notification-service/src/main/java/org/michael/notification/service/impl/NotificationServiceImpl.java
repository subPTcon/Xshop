package org.michael.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.michael.notification.mapper.NotificationMapper;
import org.michael.notification.pojo.Notification;
import org.michael.notification.service.NotificationService;
import org.michael.notification.vo.NotificationPageVO;
import org.michael.notification.vo.NotificationVO;
import org.springframework.stereotype.Service;

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
}
