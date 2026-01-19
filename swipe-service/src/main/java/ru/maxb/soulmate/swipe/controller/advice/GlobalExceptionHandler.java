package ru.maxb.soulmate.swipe.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.maxb.soulmate.swipe.dto.ErrorResponse;
import ru.maxb.soulmate.swipe.exception.AuthenticationException;
import ru.maxb.soulmate.swipe.exception.SwipeException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SwipeException.class)
    public ResponseEntity<ErrorResponse> handleSwipeException(SwipeException ex) {
        String errorId = UUID.randomUUID().toString();
        log.warn("SwipeException [{}]: {}", errorId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse()
                .error(errorId)
                .status(String.valueOf(BAD_REQUEST.value()))
                .message(ex.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String errorId = UUID.randomUUID().toString();
        log.warn("AuthenticationException [{}]: {}", errorId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse()
                .error(errorId)
                .status(String.valueOf(UNAUTHORIZED.value()))
                .message(ex.getMessage());

        return new ResponseEntity<>(error, UNAUTHORIZED);
    }

    /**
     * Catch-all for any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled exception [{}]: {}", errorId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse()
                .error(errorId)
                .status(String.valueOf(INTERNAL_SERVER_ERROR.value()))
                .message(ex.getMessage());

        return new ResponseEntity<>(error, INTERNAL_SERVER_ERROR);
    }
}
