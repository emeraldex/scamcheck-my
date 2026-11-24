package my.scamcheck.core.rule.phone;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.Rule;
import org.junit.jupiter.api.Test;

class PhoneFormatRuleTest {

    private static final InputExtractor EXTRACTOR = new InputExtractor();
    private final Rule invalid = PhoneFormatRule.invalidMalaysian();
    private final Rule international = PhoneFormatRule.international();

    private static List<Finding> evaluate(Rule rule, String text) {
        return rule.evaluate(EXTRACTOR.extract(new AnalysisInput(text)));
    }

    @Test
    void us05_ac1_validMalaysianNumbersGetNoWarning() {
        String text = "Call 012-345 6789, +60123456789 or 011-2345 6789";

        assertThat(evaluate(invalid, text)).isEmpty();
        assertThat(evaluate(international, text)).isEmpty();
    }

    @Test
    void us05_ac2_invalidMalaysianNumberIsFlagged() {
        assertThat(evaluate(invalid, "Hubungi pegawai di 012-345 678"))
                .singleElement()
                .satisfies(f -> {
                    assertThat(f.ruleId()).isEqualTo("phone.invalid-my");
                    assertThat(f.category()).isEqualTo(Category.PHONE);
                    assertThat(f.weight()).isEqualTo(Weight.MODERATE);
                    assertThat(f.matchedText()).isEqualTo("012-345 678");
                    assertThat(f.messageKey()).isEqualTo("finding.phone.invalid-my");
                });
    }

    @Test
    void us05_ac3_internationalNumberIsFlagged() {
        assertThat(evaluate(international, "WhatsApp +855 12 345 678 for details"))
                .singleElement()
                .satisfies(f -> {
                    assertThat(f.ruleId()).isEqualTo("phone.international");
                    assertThat(f.weight()).isEqualTo(Weight.WEAK);
                    assertThat(f.matchedText()).isEqualTo("+855 12 345 678");
                });
        assertThat(evaluate(invalid, "WhatsApp +855 12 345 678")).isEmpty();
    }

    @Test
    void textWithoutNumbersTriggersNothing() {
        assertThat(evaluate(invalid, "Jom makan")).isEmpty();
        assertThat(evaluate(international, "Jom makan")).isEmpty();
    }
}
