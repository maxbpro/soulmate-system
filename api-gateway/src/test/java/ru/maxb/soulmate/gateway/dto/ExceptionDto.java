package ru.maxb.soulmate.gateway.dto;

import java.time.LocalDateTime;

public record ExceptionDto(String message,
                           String code,
                           LocalDateTime timestamp,
                           String path) {
    public ExceptionDto(String message, String code, String path) {
        this(message, code, LocalDateTime.now(), path);
    }
}
