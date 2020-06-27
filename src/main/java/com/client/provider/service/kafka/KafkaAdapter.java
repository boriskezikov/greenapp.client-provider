package com.client.provider.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

import static com.client.provider.exception.InvocationError.KAFKA_INVOCATION_ERROR;

@Component
@RequiredArgsConstructor
public class KafkaAdapter {

    private final static Logger log = LoggerFactory.getLogger(KafkaAdapter.class);
    private final static String topic = "2z2j7jw9-client-event";

    private final KafkaProducer producer;
    private final ObjectMapper objectMapper;

    public Mono<Void> sendEvent(Event event) {
        var message = senderRecord(event);
        return producer.send(message)
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100)))
            .doOnSubscribe(i -> log.info("KafkaAdapter.sendEvent.in event = {}", event))
            .doOnComplete(() -> log.info("KafkaAdapter.sendEvent.out"))
            .onErrorMap(KAFKA_INVOCATION_ERROR::exception)
            .then();
    }

    @SneakyThrows
    private Mono<SenderRecord<String, String, Void>> senderRecord(Event event) {
        var record = new ProducerRecord<String, String>(topic, objectMapper.writeValueAsString(event));
        return Mono.just(SenderRecord.create(record, null));
    }

    @ToString
    @RequiredArgsConstructor
    public static class Event {

        public final String value;
        public final Long taskId;
    }
}

