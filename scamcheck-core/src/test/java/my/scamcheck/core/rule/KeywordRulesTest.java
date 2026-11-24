package my.scamcheck.core.rule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;
import org.junit.jupiter.api.Test;

/**
 * The shipped keyword data files against SRS US-03 acceptance criteria.
 * Each test name points at the criterion it covers.
 */
class KeywordRulesTest {

    private final List<Rule> rules = KeywordRules.defaults();
    private final InputExtractor extractor = new InputExtractor();

    private List<Category> categoriesFound(String text) {
        ExtractedInput input = extractor.extract(new AnalysisInput(text));
        return rules.stream()
                .flatMap(rule -> rule.evaluate(input).stream())
                .map(Finding::category)
                .distinct()
                .toList();
    }

    @Test
    void loadsFiveRulesWithUniqueIds() {
        assertThat(rules).extracting(Rule::id)
                .containsExactly(
                        "keyword.investment",
                        "keyword.urgency",
                        "keyword.account-threat",
                        "keyword.impersonation",
                        "keyword.credential-request");
    }

    @Test
    void us03_ac1_malayGuaranteedReturn() {
        assertThat(categoriesFound("Pelaburan dijamin untung 30% sebulan")).contains(Category.INVESTMENT);
    }

    @Test
    void us03_ac2_englishAccountThreatAndUrgency() {
        assertThat(categoriesFound("Your account has been suspended, act now"))
                .contains(Category.ACCOUNT_THREAT, Category.URGENCY);
    }

    @Test
    void us03_ac3_malayAccountThreatAndUrgency() {
        assertThat(categoriesFound("Akaun anda dibekukan. Sila hubungi segera"))
                .contains(Category.ACCOUNT_THREAT, Category.URGENCY);
    }

    @Test
    void us03_ac4_credentialRequest() {
        assertThat(categoriesFound("Please share the TAC/OTP you received")).contains(Category.CREDENTIAL_REQUEST);
    }

    @Test
    void us03_ac5_ignoresCaseAndExtraSpaces() {
        assertThat(categoriesFound("SEGERA")).contains(Category.URGENCY);
        assertThat(categoriesFound("akaun  dibekukan")).contains(Category.ACCOUNT_THREAT);
    }

    @Test
    void us03_ac6_beruntungDoesNotFireInvestment() {
        assertThat(categoriesFound("Saya rasa beruntung dapat kerja ini")).doesNotContain(Category.INVESTMENT);
    }

    @Test
    void genuineBankWarningAboutTacIsNotACredentialRequest() {
        assertThat(categoriesFound("Never share your TAC with anyone. Bank staff will never ask for it."))
                .doesNotContain(Category.CREDENTIAL_REQUEST);
    }

    @Test
    void ordinaryChatTriggersNothing() {
        assertThat(categoriesFound("Jom makan tengah hari esok? Aku belanja.")).isEmpty();
    }
}
