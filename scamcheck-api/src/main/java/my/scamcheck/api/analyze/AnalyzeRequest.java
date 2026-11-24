package my.scamcheck.api.analyze;

import my.scamcheck.api.error.InvalidRequestException;
import my.scamcheck.core.model.AnalysisInput;

/**
 * Body of {@code POST /api/v1/analyze} (docs/api/openapi.yaml).
 *
 * <p>Validated explicitly in {@link #validate()} rather than with Bean Validation:
 * Spring's {@code MethodArgumentNotValidException} includes the rejected value in
 * its message, and Spring MVC logs that message at DEBUG level, which would leak
 * the user's text. {@code NoInputLoggingTest} caught this (ADR-008).
 *
 * @param text     the pasted input
 * @param language language for explanations; {@code null} means {@link Language#DEFAULT}
 */
public record AnalyzeRequest(String text, Language language) {

    public Language languageOrDefault() {
        return language == null ? Language.DEFAULT : language;
    }

    /** Throws {@link InvalidRequestException} if the text is missing, blank or too long (SRS US-01). */
    public void validate() {
        if (text == null || text.isBlank()) {
            throw new InvalidRequestException("text", "Text must not be blank.");
        }
        if (text.length() > AnalysisInput.MAX_LENGTH) {
            throw new InvalidRequestException("text", "Text must be 5,000 characters or fewer.");
        }
    }

    /**
     * Never reveals the text. Spring MVC logs request bodies via toString at
     * DEBUG level, so the default record toString would leak input (ADR-008).
     */
    @Override
    public String toString() {
        return "AnalyzeRequest[length=" + (text == null ? 0 : text.length()) + ", language=" + language + "]";
    }
}
