package org.michael.logistics.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogisticsEventProducer {

    private static final String TOPIC_LOGISTICS_STATUS_CHANGED = "logistics-status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendLogisticsStatusChanged(LogisticsStatusChangedEvent event) {
        kafkaTemplate.send(
                TOPIC_LOGISTICS_STATUS_CHANGED,
                event.getOrderNo(),
                event
        );

        log.info("发送 logistics-status-changed 消息，eventId={}, orderNo={}, status={}",
                event.getEventId(),
                event.getOrderNo(),
                event.getStatus()
        );
    }
}
