package com.client.provider.logic;

import com.client.provider.model.Client.Attachment;
import com.client.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.client.provider.exception.ApplicationError.ATTACHMENTS_NOT_FOUND;
import static com.client.provider.utils.Utils.logProcess;

@Component
@RequiredArgsConstructor
public class FindAttachmentByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindAttachmentByIdOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Mono<Attachment> process(FindAttachmentsByIdRequest request) {
        return r2dbcAdapter.findAttachmentsById(request)
            .switchIfEmpty(
                ATTACHMENTS_NOT_FOUND.exceptionMono(
                    "No attachments found with id = " + request.id))
            .as(logProcess(log, "FindAttachmentsByIdOperation", request));
    }

    @ToString
    @RequiredArgsConstructor
    public static class FindAttachmentsByIdRequest {

        public final Long id;

        public Query bindOn(Query query) {
            return query.bind("$1", this.id);
        }
    }
}
