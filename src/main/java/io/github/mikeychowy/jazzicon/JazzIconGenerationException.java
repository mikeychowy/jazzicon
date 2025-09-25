package io.github.mikeychowy.jazzicon;

public class JazzIconGenerationException extends RuntimeException {
    public JazzIconGenerationException(String message) {
        super(message);
    }

    public JazzIconGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JazzIconGenerationException(Throwable cause) {
        super(cause);
    }

    public JazzIconGenerationException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public JazzIconGenerationException() {}
}
