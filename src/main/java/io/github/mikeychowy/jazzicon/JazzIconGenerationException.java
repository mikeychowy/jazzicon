package io.github.mikeychowy.jazzicon;

/**
 * A specialized {@link RuntimeException}. to indicate something went wrong during icon generation. <br>
 * <br>
 * Wrapper for any checked exceptions, also, to stop Sonarlint from breathing down my neck by wrapping it in
 * {@link RuntimeException}.
 */
@SuppressWarnings({"unused"})
@ExcludeGeneratedOrSpecialCaseFromCoverage
public class JazzIconGenerationException extends RuntimeException {
    /**
     * Constructs a new runtime exception with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *                method.
     */
    public JazzIconGenerationException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and cause.
     *
     * <p>Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A {@code null}
     *     value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public JazzIconGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a detail message of {@code (cause==null ? null :
     * cause.toString())} (which typically contains the class and detail message of {@code cause}). This constructor is
     * useful for runtime exceptions that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A {@code null}
     *     value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public JazzIconGenerationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param message the detail message.
     * @param cause the cause. (A {@code null} value is permitted, and indicates that the cause is nonexistent or
     *     unknown.)
     * @param enableSuppression whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public JazzIconGenerationException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new runtime exception with {@code null} as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     */
    public JazzIconGenerationException() {
        super();
    }
}
