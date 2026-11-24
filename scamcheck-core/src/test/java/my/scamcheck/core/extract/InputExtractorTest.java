package my.scamcheck.core.extract;

import static org.assertj.core.api.Assertions.assertThat;

import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.ExtractedInput;
import org.junit.jupiter.api.Test;

class InputExtractorTest {

    private final InputExtractor extractor = new InputExtractor();

    @Test
    void keepsOriginalTextAndAddsNormalisedText() {
        ExtractedInput extracted = extractor.extract(new AnalysisInput("  Akaun ANDA  dibekukan "));

        assertThat(extracted.originalText()).isEqualTo("  Akaun ANDA  dibekukan ");
        assertThat(extracted.normalizedText()).isEqualTo("akaun anda dibekukan");
    }

    @Test
    void toStringDoesNotRevealInput() {
        ExtractedInput extracted = extractor.extract(new AnalysisInput("secret 0123456789"));

        assertThat(extracted.toString()).doesNotContain("secret", "0123456789");
    }
}
