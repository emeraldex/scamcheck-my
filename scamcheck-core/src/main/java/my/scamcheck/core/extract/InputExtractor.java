package my.scamcheck.core.extract;

import java.util.Objects;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.ExtractedInput;

/**
 * Prepares the input once, before any rule runs (ADR-002): normalises the text
 * for keyword rules and pulls out links for link rules. Phone and account
 * extraction follow in a later M4 change.
 */
public class InputExtractor {

    private final UrlExtractor urlExtractor;

    /** Uses the default recognised top-level domains. */
    public InputExtractor() {
        this(UrlExtractor.withDefaultTlds());
    }

    public InputExtractor(UrlExtractor urlExtractor) {
        this.urlExtractor = Objects.requireNonNull(urlExtractor, "urlExtractor");
    }

    public ExtractedInput extract(AnalysisInput input) {
        Objects.requireNonNull(input, "input");
        String text = input.text();
        return new ExtractedInput(text, TextNormalizer.normalize(text), urlExtractor.extract(text));
    }
}
