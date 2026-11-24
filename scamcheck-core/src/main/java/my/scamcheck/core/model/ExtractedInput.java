package my.scamcheck.core.model;

import java.util.Objects;

/**
 * The input after extraction, shared by every rule (ADR-002).
 *
 * <p>Milestone M3 only needs the normalised text for keyword rules. URL, phone
 * and account candidates are added in M4 when the rules that use them arrive.
 *
 * @param originalText   the text exactly as submitted
 * @param normalizedText lower-cased, Unicode-normalised, single-spaced text that
 *                       keyword rules match against
 */
public record ExtractedInput(String originalText, String normalizedText) {

    public ExtractedInput {
        Objects.requireNonNull(originalText, "originalText");
        Objects.requireNonNull(normalizedText, "normalizedText");
    }

    @Override
    public String toString() {
        return "ExtractedInput[length=" + originalText.length() + "]";
    }
}
