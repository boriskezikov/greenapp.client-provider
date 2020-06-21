package com.client.provider.model;

import io.r2dbc.spi.Row;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@ToString
@EqualsAndHashCode(exclude = {"id", "updated", "created"})
@RequiredArgsConstructor
public class Client {

    public final Long id;
    public final String login;
    public final String name;
    public final String surname;
    public final LocalDate birthDate;
    public final String description;
    public final Type type;
    public final LocalDateTime updated;
    public final LocalDateTime created;

    public static Client fromGetByIdRow(Row row) {
        return Client.builder()
            .id(row.get("id", Long.class))
            .login(row.get("login", String.class))
            .name(row.get("name", String.class))
            .surname(row.get("surname", String.class))
            .description(row.get("description", String.class))
            .type(Type.valueOf(row.get("type", String.class)))
            .birthDate(row.get("birth_date", LocalDate.class))
            .updated(row.get("updated", LocalDateTime.class))
            .created(row.get("created", LocalDateTime.class))
            .build();
    }

    public static Client fromFindRow(Row row) {
        return Client.builder()
            .id(row.get("id", Long.class))
            .name(row.get("name", String.class))
            .surname(row.get("surname", String.class))
            .type(Type.valueOf(row.get("type", String.class)))
            .build();
    }

    @RequiredArgsConstructor
    @Builder
    @EqualsAndHashCode(of = "content")
    public static class Attachment {

        public final Long id;
        public final Long clientId;
        public final Long contentLength;
        public final String contentType;
        public final byte[] content;

        public static Attachment fromRow(Row row) {
            return Attachment.builder()
                .id(row.get("id", Long.class))
                .clientId(row.get("client_id", Long.class))
                .contentLength(row.get("length", Long.class))
                .contentType(row.get("type", String.class))
                .content(row.get("content", byte[].class))
                .build();
        }
    }
}
