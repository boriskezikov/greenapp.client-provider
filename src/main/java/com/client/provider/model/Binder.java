package com.client.provider.model;

import io.r2dbc.client.Query;
import io.r2dbc.client.Update;

import static java.util.Objects.isNull;

public abstract class Binder {

    protected void bind(Update update, String identifier, Class<?> type, Object value) {
        if (isNull(value)) {
            update.bindNull(identifier, type);
        } else {
            update.bind(identifier, value);
        }
    }

    protected void bind(Query update, String identifier, Class<?> type, Object value) {
        if (isNull(value)) {
            update.bindNull(identifier, type);
        } else {
            update.bind(identifier, value);
        }
    }
}
