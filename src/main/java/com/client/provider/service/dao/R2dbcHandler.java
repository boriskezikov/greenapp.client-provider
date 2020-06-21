package com.client.provider.service.dao;

import com.client.provider.exception.HttpCodeException;
import io.r2dbc.client.Handle;
import io.r2dbc.client.R2dbc;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static com.client.provider.exception.InvocationError.POSTGRES_INVOCATION_ERROR;
import static java.util.function.Predicate.not;

@Component
@RequiredArgsConstructor
public class R2dbcHandler {

    private final R2dbc r2dbc;

    public <T> Flux<T> inTx(Function<Handle, ? extends Publisher<T>> process) {
        return this.r2dbc.inTransaction(process)
            .onErrorMap(not(HttpCodeException.class::isInstance), POSTGRES_INVOCATION_ERROR::exception);
    }

    public <T> Mono<T> inTxMono(Function<Handle, Mono<T>> process) {
        return this.r2dbc.inTransaction(process).collectList()
            .flatMap(l -> l.isEmpty()
                          ? Mono.empty()
                          : Mono.just(l.get(0)))
            .onErrorMap(not(HttpCodeException.class::isInstance), POSTGRES_INVOCATION_ERROR::exception);
    }

    public <T> Mono<T> withHandle(Function<Handle, Mono<T>> resourceFunction) {
        return this.r2dbc.withHandle(resourceFunction).collectList()
            .flatMap(l -> l.isEmpty()
                          ? Mono.empty()
                          : Mono.just(l.get(0)))
            .onErrorMap(not(HttpCodeException.class::isInstance), POSTGRES_INVOCATION_ERROR::exception);
    }

    public <T> Flux<T> withHandleFlux(Function<Handle, ? extends Publisher<T>> resourceFunction) {
        return this.r2dbc.withHandle(resourceFunction)
            .onErrorMap(not(HttpCodeException.class::isInstance), POSTGRES_INVOCATION_ERROR::exception);
    }
}

