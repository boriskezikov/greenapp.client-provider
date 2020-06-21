package com.client.provider.logic;

import com.client.provider.logic.CreateClientOperation.AttachPhotoRequest;
import com.client.provider.logic.FindClientByIdOperation.FindClientByIdRequest;
import com.client.provider.model.Binder;
import com.client.provider.model.Client;
import com.client.provider.service.dao.R2dbcAdapter;
import com.client.provider.service.dao.R2dbcHandler;
import com.client.provider.service.kafka.KafkaAdapter;
import com.client.provider.service.kafka.KafkaAdapter.Event;
import io.r2dbc.client.Update;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static com.client.provider.exception.ApplicationError.CLIENT_NOT_FOUND_BY_ID;
import static com.client.provider.utils.Utils.logProcess;

@Component
@RequiredArgsConstructor
public class EditClientOperation {

    private final static Logger log = LoggerFactory.getLogger(EditClientOperation.class);

    private final R2dbcAdapter r2dbcAdapter;
    private final R2dbcHandler r2dbcHandler;
    private final KafkaAdapter kafkaAdapter;

    public Mono<Void> process(EditClientRequest request) {
        var id = request.newClient.id;
        var oldClient = r2dbcAdapter.findById(new FindClientByIdRequest(id))
            .switchIfEmpty(CLIENT_NOT_FOUND_BY_ID.exceptionMono("No such client exist with id = " + id));
        return request.asMono()
            .zipWith(oldClient)
            .flatMap(t -> t.getT1().updateRequest(t.getT2()))
            .flatMap(r -> r2dbcHandler.inTxMono(h -> {
                var updateClient = r2dbcAdapter.update(h, r);
                var detach = request.detach ?
                             r2dbcAdapter.detach(h, id) :
                             Mono.empty();
                var attach = request.attachPhotoRequest.asMono()
                    .flatMap(a -> r2dbcAdapter.attach(h, a));
                var sendEvent = kafkaAdapter.sendEvent(new Event("ClientUpdated", id));
                return Mono.when(updateClient, detach, attach, sendEvent);
            }))
            .as(logProcess(log, "EditClientOperation", request));
    }

    @ToString(exclude = "attachPhotoRequest")
    @RequiredArgsConstructor
    public static class EditClientRequest {

        public final Client newClient;
        public final AttachPhotoRequest attachPhotoRequest;
        public final boolean detach;

        public Mono<EditClientRequest> asMono() {
            return Mono.just(this);
        }

        public Mono<UpdateClientRequest> updateRequest(Client oldClient) {
            if (oldClient.equals(this.newClient)) {
                return Mono.empty();
            }
            return new UpdateClientRequest(newClient).asMono();
        }
    }

    @ToString
    @RequiredArgsConstructor
    public static class UpdateClientRequest extends Binder {

        public final Client newClient;

        public Mono<UpdateClientRequest> asMono() {
            return Mono.just(this);
        }

        public Update bindOn(Update query) {
            bind(query, "$1", String.class, newClient.name);
            bind(query, "$2", String.class, newClient.surname);
            bind(query, "$3", String.class, newClient.login);
            bind(query, "$4", String.class, newClient.description);
            bind(query, "$5", LocalDate.class, newClient.birthDate);
            bind(query, "$6", Long.class, newClient.id);
            return query;
        }
    }
}
