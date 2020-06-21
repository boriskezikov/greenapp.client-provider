package com.client.provider.logic;

import com.client.provider.model.Client;
import com.client.provider.model.Type;
import com.client.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.StringJoiner;

import static com.client.provider.utils.Utils.logProcessFlux;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;

@Component
@RequiredArgsConstructor
public class FindClientsOperation {

    private final static Logger log = LoggerFactory.getLogger(FindClientsOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Flux<Client> process(FindClientsRequest request) {
        return r2dbcAdapter.findList(request)
            .as(logProcessFlux(log, "FindClientsOperation", request));
    }

    @ToString
    public static class FindClientsRequest {

        public final String name;
        public final String surname;
        public final Type type;
        public final String login;
        public final Long offset;
        public final Long limit;

        public FindClientsRequest(String name, String surname, Type type, String login, Long offset, Long limit) {
            this.name = name;
            this.surname = surname;
            this.type = type;
            this.login = login;
            this.offset = requireNonNullElse(offset, 0L);
            this.limit = requireNonNullElse(limit, 20L);
        }

        public Query bindOn(Query query) {
            var pos = 1;

            if (nonNull(name)) {
                query.bind(format("$%s", pos++), name);
            }
            if (nonNull(surname)) {
                query.bind(format("$%s", pos++), surname);
            }
            if (nonNull(login)) {
                query.bind(format("$%s", pos), login);
            }
            if (nonNull(type)) {
                query.bind(format("$%s", pos), type.toString());
            }
            return query;
        }

        public String appendSqlOver(String sql) {
            return sql.concat(where());
        }

        private String where() {
            var pos = 1;
            var params = new StringJoiner(" AND ");
            params.add(" WHERE id > " + offset.toString());

            if (nonNull(name)) {
                params.add(format("name = $%d", pos++));
            }
            if (nonNull(surname)) {
                params.add(format("surname = $%d", pos++));
            }
            if (nonNull(login)) {
                params.add(format("login = $%d", pos));
            }
            if (nonNull(type)) {
                params.add(format("type = $%d::client_type", pos));
            }

            return params.toString()
                .concat(format(" FETCH FIRST %d ROWS ONLY", limit));
        }
    }
}
