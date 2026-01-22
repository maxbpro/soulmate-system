package ru.maxb.soulmate.gateway.exception;

public class ProfileServiceException extends RuntimeException {

    public ProfileServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileServiceException(String message) {
        super(message);
    }
}
