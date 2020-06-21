package com.client.provider.exception;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Logger {

    public static void logThrown(Throwable e, String method) {
        var log = LoggerFactory.getLogger(method);
        if (e instanceof ApplicationError.ApplicationErrorException) {
            log.warn(method + ".process.thrown {}", e.getMessage());
        } else {
            log.warn(method + ".process.thrown ", e);
        }
    }
}
