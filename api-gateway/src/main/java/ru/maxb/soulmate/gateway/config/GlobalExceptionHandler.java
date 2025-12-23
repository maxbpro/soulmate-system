package ru.maxb.soulmate.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.maxb.soulmate.gateway.exception.ApiException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Void> handleResourceNotFoundException(ApiException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

}
