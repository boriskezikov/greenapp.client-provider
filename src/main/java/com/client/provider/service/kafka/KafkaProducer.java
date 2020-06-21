package com.client.provider.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final static String topic = "2z2j7jw9-default";

    private final KafkaTemplate<String, String> kafkaSender;

    Mono<SendResult<String, String>> send(String msg) {
        return Mono.fromFuture(kafkaSender.send(topic, msg).completable());
    }
}
