package org.michael.notification.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.notification.mapper.NotificationMapper;
import org.michael.notification.pojo.Notification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogisticsStatusChangedConsumer {

    private final NotificationMapper notificationMapper;

    @KafkaListener(
            topics = "logistics-status-changed",
            groupId = "notification-service"
    )
    public void consume(LogisticsStatusChangedEvent event) {
        log.info(
                "收到 logistics-status-changed 消息， eventId={}, orderNo={}, status={}",
                event.getEventId(),
                event.getOrderNo(),
                event.getStatus()
        );
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(
                                Notification::getEventId,
                                event.getEventId()
                        )
        );
        if (count > 0) {
            log.info("物流状态消息已处理，忽略重复消息，eventId={}", event.getEventId());
            return;
        }
        Notification notification = new Notification();
        notification.setEventId(event.getEventId());
        notification.setUserId(event.getUserId());
        notification.setType(3);
        notification.setBizType("logistics");
        notification.setBizNo(event.getLogisticsNo());
        notification.setTitle(buildTitle(event.getStatus()));
        notification.setContent(buildContent(event));
        notification.setIsRead(0);
        notification.setSendStatus(1);
        notification.setRetryCount(0);
        notificationMapper.insert(notification);
        log.info(
                "物流通知保存成功, eventId={}, orderNo={}",
                event.getEventId(),
                event.getOrderNo()
        );
    }

    private String buildTitle(Integer status) {
        if (Integer.valueOf(1).equals(status)) {
            return "订单已发货";
        }
        if (Integer.valueOf(2).equals(status)) {
            return "物流运输中";
        }
        if (Integer.valueOf(3).equals(status)) {
            return "订单已签收";
        }

        return "物流状态更新";
    }

    private String buildContent(LogisticsStatusChangedEvent event) {
        return "订单 "
                + event.getOrderNo()
                + " 物流状态已更新: "
                + event.getDescription()
                + ", 当前位置: "
                + event.getLocation();
    }
}
