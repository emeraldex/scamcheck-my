package my.scamcheck.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import my.scamcheck.core.extract.InputExtractor;
import my.scamcheck.core.model.AnalysisInput;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.ExtractedInput;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.RiskAssessment;
import my.scamcheck.core.model.RiskLevel;
import my.scamcheck.core.model.Weight;
import my.scamcheck.core.rule.DefaultRules;
import my.scamcheck.core.rule.Rule;
import my.scamcheck.core.score.RiskScorer;
import my.scamcheck.core.score.ScoringPolicy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ScamAnalyzerTest {

    private static final RiskScorer SCORER = new RiskScorer(ScoringPolicy.defaults());

    /** End to end with the shipped keyword rules. */
    @Nested
    class WithDefaultRules {

        private final ScamAnalyzer analyzer = new ScamAnalyzer(new InputExtractor(), DefaultRules.all(), SCORER);

        private RiskAssessment analyze(String text) {
            return analyzer.analyze(new AnalysisInput(text));
        }

        @Test
        void casualUseOfUntungIsLow() { // ARCHITECTURE §4.3 example 1, R2
            RiskAssessment result = analyze("Jom makan, untung la dapat diskaun");

            assertThat(result.level()).isEqualTo(RiskLevel.LOW);
            assertThat(result.score()).isEqualTo(10);
        }

        @Test
        void frozenAccountWithUrgencyIsMedium() { // ARCHITECTURE §4.3 example 2
            RiskAssessment result = analyze("Akaun anda dibekukan, hubungi segera");

            assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
            assertThat(result.score()).isEqualTo(55);
            assertThat(result.findings())
                    .extracting(Finding::matchedText)
                    .containsExactly("akaun anda dibekukan", "segera");
        }

        @Test
        void investmentScamWithCredentialRequestIsHigh() {
            RiskAssessment result = analyze(
                    "Pelaburan dijamin untung! Daftar segera dan berikan kod TAC untuk pengesahan.");

            assertThat(result.level()).isEqualTo(RiskLevel.HIGH);
            assertThat(result.findings())
                    .extracting(Finding::category)
                    .contains(Category.INVESTMENT, Category.URGENCY, Category.CREDENTIAL_REQUEST);
        }

        @Test
        void englishPoliceImpersonationScamIsMediumWithKeywordsAlone() {
            // impersonation STRONG 35 + urgency MODERATE 20 = 55. The "transfer your savings"
            // request has no keyword category yet; Phase 5 tuning against the sample sets
            // decides whether this should reach HIGH (ADR-005).
            RiskAssessment result = analyze(
                    "This is a police officer. There is an arrest warrant in your name for money laundering. "
                            + "Act now and transfer your savings for checking.");

            assertThat(result.score()).isEqualTo(55);
            assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
        }

        @Test
        void investmentScamWithLookalikeBankLinkIsHigh() { // ARCHITECTURE §4.3 example 3, SRS US-02 AC4
            RiskAssessment result = analyze("Pelaburan dijamin untung 30%, daftar segera di maybank2u-secure.xyz");

            // investment STRONG 35 + urgency MODERATE 20 + link STRONG 35 (lookalike; .xyz is also LINK)
            assertThat(result.score()).isEqualTo(90);
            assertThat(result.level()).isEqualTo(RiskLevel.HIGH);
            assertThat(result.findings()).extracting(Finding::ruleId)
                    .contains("link.lookalike-bank", "link.unusual-tld");
        }

        @Test
        void lookalikeBankLinkAloneIsMedium() { // ARCHITECTURE §4.3 example 4
            RiskAssessment result = analyze("https://maybank2u-secure.xyz/login");

            assertThat(result.score()).isEqualTo(35);
            assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
        }

        @Test
        void genuineBankLinkIsLow() {
            RiskAssessment result = analyze("Log in at https://www.maybank2u.com.my to see your statement.");

            assertThat(result.level()).isEqualTo(RiskLevel.LOW);
            assertThat(result.findings()).isEmpty();
        }

        @Test
        void accountNumberAloneIsLowButReported() { // SRS US-06 AC1, AC4
            RiskAssessment result = analyze("No akaun saya 5123 4567 8901");

            assertThat(result.level()).isEqualTo(RiskLevel.LOW);
            assertThat(result.score()).isEqualTo(5);
            assertThat(result.findings()).extracting(Finding::ruleId).containsExactly("account.detected");
        }

        @Test
        void urgentTransferToAccountIsMedium() {
            RiskAssessment result = analyze("Bank in segera ke akaun 5123 4567 8901");

            // urgency MODERATE 20 + account INFO 5 = 25
            assertThat(result.score()).isEqualTo(25);
            assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
        }

        @Test
        void impersonationWithOverseasNumberIsReported() {
            RiskAssessment result = analyze("Pegawai polis di sini. Hubungi +855 12 345 678 dengan segera.");

            assertThat(result.findings()).extracting(Finding::ruleId)
                    .contains("keyword.impersonation", "keyword.urgency", "phone.international");
            // impersonation 20 + urgency 20 + phone 10 = 50
            assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
        }

        @Test
        void ordinaryMessageIsLowWithNoFindings() { // SRS US-02 AC2
            RiskAssessment result = analyze("Jom makan tengah hari esok?");

            assertThat(result.level()).isEqualTo(RiskLevel.LOW);
            assertThat(result.findings()).isEmpty();
        }
    }

    @Nested
    class Wiring {

        private final Rule alwaysFires = new FixedRule("test.always", Category.URGENCY, Weight.STRONG);

        @Test
        void runsEveryRuleAndScoresTheirFindings() {
            Rule another = new FixedRule("test.another", Category.LINK, Weight.STRONG);
            var analyzer = new ScamAnalyzer(new InputExtractor(), List.of(alwaysFires, another), SCORER);

            RiskAssessment result = analyzer.analyze(new AnalysisInput("anything"));

            assertThat(result.findings()).extracting(Finding::ruleId).containsExactlyInAnyOrder("test.always", "test.another");
            assertThat(result.level()).isEqualTo(RiskLevel.HIGH);
        }

        @Test
        void rejectsNoRules() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ScamAnalyzer(new InputExtractor(), List.of(), SCORER));
        }

        @Test
        void rejectsDuplicateRuleIds() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ScamAnalyzer(new InputExtractor(), List.of(alwaysFires, alwaysFires), SCORER))
                    .withMessageContaining("test.always");
        }
    }

    /** A hand-made rule: shows that new rules plug in without touching the engine (NFR-06). */
    private record FixedRule(String id, Category category, Weight weight) implements Rule {
        @Override
        public List<Finding> evaluate(ExtractedInput input) {
            return List.of(new Finding(id, category, weight, "x", "finding." + id, List.of()));
        }
    }
}
