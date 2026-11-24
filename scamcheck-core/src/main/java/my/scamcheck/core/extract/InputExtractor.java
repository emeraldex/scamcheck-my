package my.scamcheck.core.extract;

import java.util.Objects;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.ExtractedInput;

/**
 * Prepares the input once, before any rule runs (ADR-002): normalises the text
 * for keyword rules, and pulls out links, phone numbers and account numbers.
 */
public class InputExtractor {

    private final UrlExtractor urlExtractor;
    private final NumberExtractor numberExtractor;

    /** Uses the default recognised top-level domains. */
    public InputExtractor() {
        this(UrlExtractor.withDefaultTlds(), new NumberExtractor());
    }

    public InputExtractor(UrlExtractor urlExtractor, NumberExtractor numberExtractor) {
        this.urlExtractor = Objects.requireNonNull(urlExtractor, "urlExtractor");
        this.numberExtractor = Objects.requireNonNull(numberExtractor, "numberExtractor");
    }

    public ExtractedInput extract(AnalysisInput input) {
        Objects.requireNonNull(input, "input");
        String text = input.text();
        NumberExtractor.Result numbers = numberExtractor.extract(text);
        return new ExtractedInput(
                text,
                TextNormalizer.normalize(text),
                urlExtractor.extract(text),
                numbers.phones(),
                numbers.accounts());
    }
}
