package com.client.provider.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaSender<String, String> kafkaSender;

    Flux<SenderResult<Void>> send(Mono<SenderRecord<String, String, Void>> msg) {
        return kafkaSender.send(msg);
    }
}
