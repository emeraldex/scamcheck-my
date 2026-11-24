package my.scamcheck.core.model;

import java.util.List;
import java.util.Objects;

/**
 * The input after extraction, shared by every rule (ADR-002).
 *
 * <p>Phone and account candidates are added in a later M4 change, together with
 * the rules that use them.
 *
 * @param originalText   the text exactly as submitted
 * @param normalizedText lower-cased, Unicode-normalised, single-spaced text that
 *                       keyword rules match against
 * @param urls           links found in the text, in order of appearance, without duplicates
 */
public record ExtractedInput(String originalText, String normalizedText, List<UrlCandidate> urls) {

    public ExtractedInput {
        Objects.requireNonNull(originalText, "originalText");
        Objects.requireNonNull(normalizedText, "normalizedText");
        urls = List.copyOf(urls);
    }

    @Override
    public String toString() {
        return "ExtractedInput[length=" + originalText.length() + ", urls=" + urls.size() + "]";
    }
}
