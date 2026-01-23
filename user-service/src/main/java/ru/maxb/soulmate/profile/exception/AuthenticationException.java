package ru.maxb.soulmate.profile.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Object... args) {
        super(String.format(message, args));
    }
}
