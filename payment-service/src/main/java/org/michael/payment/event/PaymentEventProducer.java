package org.michael.payment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private static final String TOPIC_PAYMENT_SUCCESS = "payment-success";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentSuccess(PaymentSuccessEvent event) {
        kafkaTemplate.send(
                TOPIC_PAYMENT_SUCCESS,
                event.getOrderNo(),
                event
        );

        log.info("发送 payment-success 消息， eventId={}, orderNo={}, paymentNo={}", event.getEventId(), event.getOrderNo(), event.getPaymentNo());

    }
}
