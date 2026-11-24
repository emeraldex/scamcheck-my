package my.scamcheck.core.model;

import java.util.Objects;

/**
 * The text a user asked us to check.
 *
 * <p>{@link #toString()} never reveals the text, so an accidental log statement
 * or exception message can't leak it (ADR-008).
 */
public record AnalysisInput(String text) {

    /** Maximum input length in characters (SRS US-01 AC4). */
    public static final int MAX_LENGTH = 5_000;

    public AnalysisInput {
        Objects.requireNonNull(text, "text");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        if (text.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("text must be at most " + MAX_LENGTH + " characters");
        }
    }

    @Override
    public String toString() {
        return "AnalysisInput[length=" + text.length() + "]";
    }
}
