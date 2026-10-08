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
public class PaymentSuccessConsumer {

    private final NotificationMapper notificationMapper;

    @KafkaListener(
            topics = "payment-success",
            groupId = "notification-service"
    )
    public void consume(PaymentSuccessEvent event) {
        log.info("收到 payment-success 消息， eventId={}, orderNo={}, paymentNo={}",
                event.getEventId(),
                event.getOrderNo(),
                event.getPaymentNo()
        );

        // 1.消费幂等
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(
                                Notification::getEventId,
                                event.getEventId()
                        )
        );
        if (count > 0) {
            log.info("payment-success 已处理，忽略重复消息, eventId={}", event.getEventId());
            return;
        }

        // 2.创建通知
        Notification notification = new Notification();
        notification.setEventId(event.getEventId());
        notification.setUserId(event.getUserId());
        notification.setType(2);
        notification.setBizType("payment");
        notification.setBizNo(event.getPaymentNo());
        notification.setTitle("支付成功");
        notification.setContent("订单" + event.getOrderNo() + "已支付成功，等待商家发货");
        notification.setIsRead(0);
        notification.setSendStatus(1);
        notification.setRetryCount(0);
        notificationMapper.insert(notification);
        log.info("支付成功通知保存完成, eventId={}, orderNo={}", event.getEventId(), event.getOrderNo());
    }
}
