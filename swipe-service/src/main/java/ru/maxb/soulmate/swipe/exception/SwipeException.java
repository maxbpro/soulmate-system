package ru.maxb.soulmate.swipe.exception;

public class SwipeException extends RuntimeException {

    public SwipeException(String message) {
        super(message);
    }

    public SwipeException(String message, Object... args) {
        super(String.format(message, args));
    }
}
