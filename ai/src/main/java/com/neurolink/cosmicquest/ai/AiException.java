package com.neurolink.cosmicquest.ai;

/**
 * Thrown when an AI provider gives no usable questions: no key, no
 * connection, a timeout, an error status such as HTTP 429, or a reply that
 * is not the expected JSON. The game catches it and uses local questions.
 */
public class AiException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message what went wrong, shown in logs and tests.
     */
    public AiException(String message) {
        super(message);
    }

    /**
     * Creates the error with the problem that caused it.
     *
     * @param message what went wrong.
     * @param cause the original problem, such as an IOException.
     */
    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
