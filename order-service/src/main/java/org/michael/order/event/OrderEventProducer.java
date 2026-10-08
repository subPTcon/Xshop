package org.michael.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC_ORDER_CREATED = "order-created";

    private static final String TOPIC_ORDER_CANCELLED = "order-cancelled";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(
                TOPIC_ORDER_CREATED,
                event.getOrderNo(),
                event
        );
    }

    public void sendOrderCancelled(OrderCancelledEvent event) {
        kafkaTemplate.send(
                TOPIC_ORDER_CANCELLED,
                event.getOrderNo(),
                event
        );
        log.info(
                "发送 order-cancelled 消息, eventId={}, orderNo={}",
                event.getEventId(),
                event.getOrderNo()
        );

    }
}
