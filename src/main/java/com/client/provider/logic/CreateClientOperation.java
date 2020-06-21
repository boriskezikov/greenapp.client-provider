package com.client.provider.logic;

import com.client.provider.model.Binder;
import com.client.provider.model.Client;
import com.client.provider.service.dao.R2dbcAdapter;
import com.client.provider.service.dao.R2dbcHandler;
import com.client.provider.service.kafka.KafkaAdapter;
import com.client.provider.service.kafka.KafkaAdapter.Event;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.client.provider.exception.ValidationError.INVALID_ATTACH_REQUEST;
import static com.client.provider.utils.Utils.logProcess;
import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class CreateClientOperation {

    private final static Logger log = LoggerFactory.getLogger(CreateClientOperation.class);

    private final R2dbcAdapter r2dbcAdapter;
    private final R2dbcHandler r2dbcHandler;
    private final KafkaAdapter kafkaAdapter;

    public Mono<Long> process(CreateClientRequest request) {
        return r2dbcHandler.inTxMono(
            h -> {
                var clientIdMono = r2dbcAdapter.insert(h, request).cache();
                var attachPhotosMono = clientIdMono
                    .flatMap(id -> request.attachPhotoRequest.asMono()
                        .map(a -> {
                            a.setClientId(id);
                            return a;
                        })
                        .flatMap(AttachPhotoRequest::validate)
                        .flatMap(r -> r2dbcAdapter.attach(h, r))
                    );
                var sendEventMono = clientIdMono
                    .map(id -> new Event("ClientCreated", id))
                    .flatMap(kafkaAdapter::sendEvent);
                return Mono.when(attachPhotosMono)
                    .then(clientIdMono);
            }
        ).as(logProcess(log, "CreateClientOperation", request));
    }

    @ToString(exclude = "attachPhotoRequest")
    @RequiredArgsConstructor
    public static class CreateClientRequest extends Binder {

        public final Client newClient;
        public final AttachPhotoRequest attachPhotoRequest;

        public Query bindOn(Query query) {
            bind(query, "$1", String.class, newClient.name);
            bind(query, "$2", String.class, newClient.surname);
            bind(query, "$3", String.class, newClient.login);
            bind(query, "$4", String.class, newClient.description);
            bind(query, "$5", String.class, newClient.type.toString());
            bind(query, "$6", LocalDateTime.class, newClient.birthDate);
            return query;
        }
    }

    @Setter
    @ToString
    @RequiredArgsConstructor
    public static class AttachPhotoRequest extends Binder {

        private Long clientId;
        public final String contentType;
        public final Long contentLength;
        public final byte[] content;

        public Query bindOn(Query query) {
            query
                .bind("$1", this.clientId)
                .bind("$4", this.content);
            bind(query, "$2", String.class, this.contentType);
            bind(query, "$3", Long.class, this.contentLength);
            return query;
        }

        public Mono<AttachPhotoRequest> asMono() {
            return Mono.just(this);
        }

        public Mono<AttachPhotoRequest> validate() {
            if (isNull(clientId)) {
                return INVALID_ATTACH_REQUEST.exceptionMono("Client id cannot be null");
            } else if (isNull(contentType)) {
                return INVALID_ATTACH_REQUEST.exceptionMono("Content-Type cannot be null");
            } else if (isNull(content)) {
                return INVALID_ATTACH_REQUEST.exceptionMono("Content cannot be null");
            }
            return asMono();
        }
    }
}
