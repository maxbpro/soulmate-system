package ru.maxb.soulmate.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class KeycloakClientException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String responseBody;

    public KeycloakClientException(String message, HttpStatusCode statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public KeycloakClientException(String message, HttpStatusCode statusCode,
                                   String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

}
