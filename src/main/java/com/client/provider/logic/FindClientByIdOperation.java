package com.client.provider.logic;


import com.client.provider.model.Client;
import com.client.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.client.provider.exception.ApplicationError.CLIENT_NOT_FOUND_BY_ID;
import static com.client.provider.utils.Utils.logProcess;

@Component
@RequiredArgsConstructor
public class FindClientByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindClientByIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Client> process(FindClientByIdRequest request) {
        return r2dbcAdapter.findById(request)
            .switchIfEmpty(CLIENT_NOT_FOUND_BY_ID.exceptionMono("No such client exist with id = " + request.clientId))
            .as(logProcess(log, "FindClientByIdOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class FindClientByIdRequest {

        public final Long clientId;

        public Query bindOn(Query query) {
            return query.bind("$1", this.clientId);
        }
    }
}
