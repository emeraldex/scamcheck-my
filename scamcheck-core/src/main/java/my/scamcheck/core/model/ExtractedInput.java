package my.scamcheck.core.model;

import java.util.List;
import java.util.Objects;

/**
 * The input after extraction, shared by every rule (ADR-002).
 *
 * @param originalText   the text exactly as submitted
 * @param normalizedText lower-cased, Unicode-normalised, single-spaced text that
 *                       keyword rules match against
 * @param urls           links found in the text, in order of appearance, without duplicates
 * @param phones         phone numbers found, without duplicates
 * @param accounts       possible bank account numbers, never overlapping {@code phones}
 */
public record ExtractedInput(
        String originalText,
        String normalizedText,
        List<UrlCandidate> urls,
        List<PhoneCandidate> phones,
        List<AccountCandidate> accounts) {

    public ExtractedInput {
        Objects.requireNonNull(originalText, "originalText");
        Objects.requireNonNull(normalizedText, "normalizedText");
        urls = List.copyOf(urls);
        phones = List.copyOf(phones);
        accounts = List.copyOf(accounts);
    }

    @Override
    public String toString() {
        return "ExtractedInput[length=" + originalText.length() + ", urls=" + urls.size()
                + ", phones=" + phones.size() + ", accounts=" + accounts.size() + "]";
    }
}
