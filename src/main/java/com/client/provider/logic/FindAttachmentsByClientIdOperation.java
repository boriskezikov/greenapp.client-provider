package com.client.provider.logic;


import com.client.provider.model.Client.Attachment;
import com.client.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.client.provider.exception.ApplicationError.ATTACHMENTS_NOT_FOUND;
import static com.client.provider.utils.Utils.logProcessFlux;

@Component
@RequiredArgsConstructor
public class FindAttachmentsByClientIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindAttachmentsByClientIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Flux<Attachment> process(FindAttachmentsByClientIdRequest request) {
        return r2dbcAdapter.findAttachmentsByClientId(request)
            .switchIfEmpty(
                ATTACHMENTS_NOT_FOUND.exceptionMono(
                    "No attachments found for client with id = " + request.clientId))
            .as(logProcessFlux(log, "FindAttachmentsByClientIdOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class FindAttachmentsByClientIdRequest {

        public final Long clientId;

        public Query bindOn(Query query) {
            return query.bind("$1", this.clientId);
        }
    }
}
