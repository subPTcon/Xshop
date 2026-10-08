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
public class OrderCreatedConsumer {

    private final NotificationMapper notificationMapper;

    @KafkaListener(
            topics = "order-created",
            groupId = "notification-service"
    )
    public void consume(OrderCreatedEvent event) {
        log.info("收到 order-created 消息: {}", event);
        /**
         * 1.幂等检查
         */
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(
                                Notification::getEventId,
                                event.getEventId()
                        )
        );
        if (count > 0) {
            log.info("消息已处理，忽略重复消息，eventId={}", event.getEventId());
            return;
        }

        /**
         * 2.创建站内通知
         */
        Notification notification = new Notification();
        notification.setEventId(event.getEventId());
        notification.setUserId(event.getUserId());
        notification.setType(1);
        notification.setBizType("order");
        notification.setBizNo(event.getOrderNo());
        notification.setTitle("订单创建成功");
        notification.setContent("订单" + event.getOrderNo() + "已创建成功，请尽快完成支付");
        notification.setIsRead(0);
        notification.setSendStatus(1);
        notification.setRetryCount(0);
        notificationMapper.insert(notification);
        log.info("订单创建通知保存成功, eventId={}, orderNo={}", event.getEventId(), event.getOrderNo());

    }
}
