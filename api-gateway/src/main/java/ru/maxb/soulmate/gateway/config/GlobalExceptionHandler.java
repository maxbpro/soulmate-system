package ru.maxb.soulmate.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.maxb.soulmate.gateway.dto.ErrorResponse;
import ru.maxb.soulmate.gateway.exception.ApiException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle WebExchangeBindException (Validation errors)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        Map<String, String> details = new HashMap<>();
        ex.getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = new ErrorResponse()
                .error("Validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .type(ErrorResponse.TypeEnum.VALIDATION)
                .code(ErrorResponse.CodeEnum.VALIDATION_FAILED)
                .details(details)
                .timestamp(OffsetDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(ErrorResponse.MethodEnum.fromValue(
                        exchange.getRequest().getMethod().name()));

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    // Handle ResponseStatusException (e.g., @ResponseStatus)
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(
            ResponseStatusException ex,
            ServerWebExchange exchange) {

        ErrorResponse errorResponse = new ErrorResponse()
                .error(ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString())
                .status(ex.getStatusCode().value())
                .timestamp(OffsetDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(ErrorResponse.MethodEnum.fromValue(
                        exchange.getRequest().getMethod().name()));

        // Map specific status codes to types
        if (ex.getStatusCode().is4xxClientError()) {
            errorResponse.type(ErrorResponse.TypeEnum.fromValue(get4xxType(ex.getStatusCode())));
        } else if (ex.getStatusCode().is5xxServerError()) {
            errorResponse.type(ErrorResponse.TypeEnum.INTERNAL);
            errorResponse.code(ErrorResponse.CodeEnum.INTERNAL_ERROR);
        }

        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .body(errorResponse));
    }

    // Handle generic exceptions (fallback)
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {

        ErrorResponse errorResponse = new ErrorResponse()
                .error("Internal server error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .type(ErrorResponse.TypeEnum.INTERNAL)
                .code(ErrorResponse.CodeEnum.INTERNAL_ERROR)
                .timestamp(OffsetDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(ErrorResponse.MethodEnum.fromValue(
                        exchange.getRequest().getMethod().name()));

        // Log the actual exception (but don't expose it to clients)
        log.error("error", ex);

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }

    @ExceptionHandler({
            org.springframework.security.access.AccessDeniedException.class,
            org.springframework.security.core.AuthenticationException.class
    })
    public Mono<ResponseEntity<ErrorResponse>> handleSecurityException(
            RuntimeException ex,
            ServerWebExchange exchange) {

        ErrorResponse.TypeEnum type = ex instanceof org.springframework.security.access.AccessDeniedException
                ? ErrorResponse.TypeEnum.AUTHORIZATION
                : ErrorResponse.TypeEnum.AUTHENTICATION;

        ErrorResponse.CodeEnum code = ex instanceof org.springframework.security.access.AccessDeniedException
                ? ErrorResponse.CodeEnum.ACCESS_DENIED
                : ErrorResponse.CodeEnum.INVALID_CREDENTIALS;

        ErrorResponse errorResponse = new ErrorResponse()
                .error(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .type(type)
                .code(code)
                .timestamp(OffsetDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(ErrorResponse.MethodEnum.fromValue(
                        exchange.getRequest().getMethod().name()));

        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse));
    }

    // Helper to map 4xx status codes to types
    private String get4xxType(HttpStatusCode status) {
        return switch (status) {
            case NOT_FOUND -> ErrorResponse.TypeEnum.NOT_FOUND.getValue();
            case CONFLICT -> ErrorResponse.TypeEnum.CONFLICT.getValue();
            case TOO_MANY_REQUESTS -> ErrorResponse.TypeEnum.RATE_LIMIT.getValue();
            case REQUEST_TIMEOUT, GATEWAY_TIMEOUT -> ErrorResponse.TypeEnum.TIMEOUT.getValue();
            default -> "validation"; // Default type
        };
    }
}
