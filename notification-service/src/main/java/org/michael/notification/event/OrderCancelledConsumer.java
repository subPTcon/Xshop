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
public class OrderCancelledConsumer {

    private final NotificationMapper notificationMapper;

    @KafkaListener(
            topics = "order-cancelled",
            groupId = "notification-service"
    )
    public void consume(OrderCancelledEvent event) {
        log.info(
                "收到 order-cancelled 消息, eventId={}, orderNo={}",
                event.getEventId(),
                event.getOrderNo()
        );

        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(
                                Notification::getEventId,
                                event.getEventId()
                        )
        );
        if (count > 0) {
            log.info(
                    "order-cancelled 已处理，忽略重复消息, eventId={}",
                    event.getEventId()
            );
            return;
        }

        Notification notification = new Notification();
        notification.setEventId(event.getEventId());
        notification.setUserId(event.getUserId());
        notification.setType(4);
        notification.setBizType("order");
        notification.setBizNo(event.getOrderNo());
        notification.setTitle("订单已取消");
        notification.setContent("订单 " + event.getOrderNo() + " 已取消，原因：" + event.getReason());
        notification.setIsRead(0);
        notification.setSendStatus(1);
        notification.setRetryCount(0);
        notificationMapper.insert(notification);
        log.info(
                "订单取消通知保存成功，eventId={}, orderNo={}",
                event.getEventId(),
                event.getOrderNo()
        );
    }
}
