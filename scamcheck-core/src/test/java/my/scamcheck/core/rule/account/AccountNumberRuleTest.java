package my.scamcheck.core.rule.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.Weight;
import org.junit.jupiter.api.Test;

class AccountNumberRuleTest {

    private static final InputExtractor EXTRACTOR = new InputExtractor();
    private final AccountNumberRule rule = new AccountNumberRule();

    private List<Finding> evaluate(String text) {
        return rule.evaluate(EXTRACTOR.extract(new AnalysisInput(text)));
    }

    @Test
    void us06_ac1_accountNumberIsReportedAsInfo() {
        assertThat(evaluate("Sila bank in ke akaun 5123 4567 8901"))
                .singleElement()
                .satisfies(f -> {
                    assertThat(f.ruleId()).isEqualTo("account.detected");
                    assertThat(f.category()).isEqualTo(Category.ACCOUNT);
                    assertThat(f.weight()).isEqualTo(Weight.INFO);
                    assertThat(f.matchedText()).isEqualTo("5123 4567 8901");
                    assertThat(f.messageKey()).isEqualTo("finding.account.detected");
                });
    }

    @Test
    void us06_ac3_phoneNumberIsNotAlsoAnAccount() {
        assertThat(evaluate("Call 0123456789")).isEmpty();
    }

    @Test
    void textWithoutLongNumbersTriggersNothing() {
        assertThat(evaluate("Kod TAC anda 123456")).isEmpty();
    }
}
