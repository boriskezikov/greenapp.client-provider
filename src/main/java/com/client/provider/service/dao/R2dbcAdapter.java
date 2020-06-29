package com.client.provider.service.dao;

import com.client.provider.logic.CreateClientOperation.AttachPhotoRequest;
import com.client.provider.logic.CreateClientOperation.CreateClientRequest;
import com.client.provider.logic.EditClientOperation.UpdateClientRequest;
import com.client.provider.logic.FindAttachmentByIdOperation.FindAttachmentsByIdRequest;
import com.client.provider.logic.FindAttachmentsByClientIdOperation.FindAttachmentsByClientIdRequest;
import com.client.provider.logic.FindClientByIdOperation.FindClientByIdRequest;
import com.client.provider.logic.FindClientsOperation.FindClientsRequest;
import com.client.provider.model.Client;
import com.client.provider.model.Client.Attachment;
import io.r2dbc.client.Handle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class R2dbcAdapter {

    private final R2dbcHandler handler;

    public Mono<Client> findById(FindClientByIdRequest request) {
        return this.handler.withHandle(h -> {
            var sql = "SELECT id, name, surname, login, description, CAST(type AS VARCHAR), birth_date, updated, created " +
                "FROM public.client WHERE id = $1";
            var client = request.bindOn(h.createQuery(sql))
                .mapRow(Client::fromGetByIdRow)
                .next();
            sql = "SELECT id FROM public.attachment WHERE client_id = $1";
            var attachmentId = request.bindOn(h.createQuery(sql))
                .mapRow(r -> r.get("id", Long.class))
                .next();
            return client.flatMap(c -> attachmentId.map(c::setAttachmentId)
                .switchIfEmpty(c.asMono()));
        });
    }

    public Flux<Client> findList(FindClientsRequest request) {
        return this.handler.withHandleFlux(h -> {
            var sql = request.appendSqlOver(
                "SELECT id, name, surname, login, CAST(type AS VARCHAR) " +
                    "FROM public.client"
            );
            return request.bindOn(h.createQuery(sql))
                .mapRow(Client::fromFindRow);
        });
    }

    public Mono<Attachment> findAttachmentsById(FindAttachmentsByIdRequest request) {
        return this.handler.withHandle(h -> {
            var sql = "SELECT id, client_id, content, type, length FROM public.attachment WHERE id = $1";
            return request.bindOn(h.createQuery(sql))
                .mapRow(Attachment::fromRow)
                .next();
        });
    }

    public Flux<Attachment> findAttachmentsByClientId(FindAttachmentsByClientIdRequest request) {
        return this.handler.withHandleFlux(h -> {
            var sql = "SELECT id, client_id, content, type, length FROM public.attachment WHERE client_id = $1";
            return request.bindOn(h.createQuery(sql))
                .mapRow(Attachment::fromRow);
        });
    }

    public Mono<Long> insert(@Nullable Handle handle, CreateClientRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> insert(h, request));
        }
        var sql = "INSERT INTO public.client (name, surname, login, description, type, birth_date) " +
            "VALUES($1, $2, $3, $4, $5::client_type, $6) RETURNING id";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Long> attach(@Nullable Handle handle, AttachPhotoRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> attach(h, request));
        }
        var sql = "INSERT INTO public.attachment (client_id, type, length, content) " +
            "VALUES($1, $2, $3, $4) RETURNING id";
        return request.bindOn(handle.createQuery(sql))
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Long> detach(@Nullable Handle handle, long taskId) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> detach(h, taskId));
        }
        var sql = "DELETE FROM public.attachment WHERE client_id = $1 RETURNING id";
        return handle.createQuery(sql)
            .bind("$1", taskId)
            .mapRow(r -> r.get("id", Long.class))
            .next();
    }

    public Mono<Integer> update(@Nullable Handle handle, UpdateClientRequest request) {
        if (isNull(handle)) {
            return this.handler.withHandle(h -> update(h, request));
        }
        var sql = "UPDATE public.client SET name = $1, surname = $2, login = $3, description = $4, birth_date = $5 " +
            "WHERE id = $6";
        return request.bindOn(handle.createUpdate(sql))
            .execute()
            .next();
    }
}
