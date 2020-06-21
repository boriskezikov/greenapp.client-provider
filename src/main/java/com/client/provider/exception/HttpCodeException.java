package com.client.provider.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpCodeException extends RuntimeException {

    public final HttpResponseStatus status;
    public final String body;

    public HttpCodeException(int status, String description) {
        this(HttpResponseStatus.valueOf(status), description);
    }

    public HttpCodeException(HttpResponseStatus status, String body) {
        super(body);
        this.status = status;
        this.body = body;
    }

    public HttpCodeException(Throwable cause, int status, String body) {
        super(cause);
        this.status = HttpResponseStatus.valueOf(status);
        this.body = body;
    }

    @Override
    public String toString() {
        return "'" + body + "'";
    }
}
