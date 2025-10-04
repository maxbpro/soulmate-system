package ru.maxb.soulmate.gateway.exception;

import static java.lang.String.format;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Object... args) {
        super(format(message, args));
    }
}
