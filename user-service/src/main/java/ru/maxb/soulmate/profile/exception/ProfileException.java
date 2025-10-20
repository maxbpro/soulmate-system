package ru.maxb.soulmate.profile.exception;

public class ProfileException extends RuntimeException {

    public ProfileException(String message) {
        super(message);
    }

    public ProfileException(String message, Object... args) {
        super(String.format(message, args));
    }
}
