package my.scamcheck.core.extract;

import java.util.Objects;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.ExtractedInput;

/**
 * Prepares the input once, before any rule runs (ADR-002).
 *
 * <p>In M3 this only normalises the text. M4 adds URL, phone and account
 * extraction here, so that link rules receive parsed URLs and phone digits are
 * never reported again as an account number.
 */
public class InputExtractor {

    public ExtractedInput extract(AnalysisInput input) {
        Objects.requireNonNull(input, "input");
        return new ExtractedInput(input.text(), TextNormalizer.normalize(input.text()));
    }
}
