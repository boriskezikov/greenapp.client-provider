package com.client.provider.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.r2dbc.spi.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@ToString
@Getter
@EqualsAndHashCode(exclude = {"id", "updated", "created"})
@AllArgsConstructor
public class Client {

    public final Long id;
    public final String login;
    public final String name;
    public final String surname;
    public final LocalDate birthDate;
    public final String description;
    private Long attachmentId;
    public final Type type;
    public final LocalDateTime updated;
    public final LocalDateTime created;

    @JsonCreator
    public Client(@JsonProperty("id") Long id,
                  @JsonProperty("login") String login,
                  @JsonProperty("name") String name,
                  @JsonProperty("surname") String surname,
                  @JsonProperty("birthDate") LocalDate birthDate,
                  @JsonProperty("description") String description,
                  @JsonProperty("type") Type type,
                  @JsonProperty("updated") LocalDateTime updated,
                  @JsonProperty("created") LocalDateTime created) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.description = description;
        this.type = type;
        this.updated = updated;
        this.created = created;
    }

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

    public Client setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
        return this;
    }

    @Builder
    @EqualsAndHashCode(of = "content")
    public static class Attachment {

        public final Long id;
        public final Long clientId;
        public final Long contentLength;
        public final String contentType;
        public final byte[] content;

        public Attachment(@JsonProperty("id") Long id,
                          @JsonProperty("clientId") Long clientId,
                          @JsonProperty("contentLength") Long contentLength,
                          @JsonProperty("contentType") String contentType,
                          @JsonProperty("content") byte[] content) {
            this.id = id;
            this.clientId = clientId;
            this.contentLength = contentLength;
            this.contentType = contentType;
            this.content = content;
        }

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
