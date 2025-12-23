package ru.maxb.soulmate.landmark.exception;

public class LandmarkException extends RuntimeException {

    public LandmarkException(String message) {
        super(message);
    }

    public LandmarkException(String message, Object... args) {
        super(String.format(message, args));
    }
}
