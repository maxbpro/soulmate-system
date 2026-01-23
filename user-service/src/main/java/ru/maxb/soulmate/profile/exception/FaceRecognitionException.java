package ru.maxb.soulmate.profile.exception;

public class FaceRecognitionException extends RuntimeException {

    public FaceRecognitionException(String message) {
        super(message);
    }

    public FaceRecognitionException(String message, Object... args) {
        super(String.format(message, args));
    }
}
