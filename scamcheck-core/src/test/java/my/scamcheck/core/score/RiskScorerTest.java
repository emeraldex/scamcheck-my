package my.scamcheck.core.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.RiskAssessment;
import my.scamcheck.core.model.RiskLevel;
import my.scamcheck.core.model.Weight;
import org.junit.jupiter.api.Test;

class RiskScorerTest {

    private final RiskScorer scorer = new RiskScorer(ScoringPolicy.defaults());

    private static Finding finding(Category category, Weight weight) {
        return new Finding("test." + category, category, weight, "text", "finding.test", List.of());
    }

    @Test
    void noFindingsIsLowWithZeroScore() {
        RiskAssessment result = scorer.score(List.of());

        assertThat(result.level()).isEqualTo(RiskLevel.LOW);
        assertThat(result.score()).isZero();
        assertThat(result.findings()).isEmpty();
    }

    @Test
    void singleWeakFindingIsLow() { // SRS US-02 AC3, ARCHITECTURE §4.3 example 1
        RiskAssessment result = scorer.score(List.of(finding(Category.INVESTMENT, Weight.WEAK)));

        assertThat(result.score()).isEqualTo(10);
        assertThat(result.level()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void strongAccountThreatPlusModerateUrgencyIsMedium() { // ARCHITECTURE §4.3 example 2
        RiskAssessment result = scorer.score(List.of(
                finding(Category.ACCOUNT_THREAT, Weight.STRONG),
                finding(Category.URGENCY, Weight.MODERATE)));

        assertThat(result.score()).isEqualTo(55);
        assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void severalStrongIndependentSignsAreHigh() { // SRS US-02 AC4, ARCHITECTURE §4.3 example 3
        RiskAssessment result = scorer.score(List.of(
                finding(Category.INVESTMENT, Weight.STRONG),
                finding(Category.URGENCY, Weight.MODERATE),
                finding(Category.LINK, Weight.STRONG)));

        assertThat(result.score()).isEqualTo(90);
        assertThat(result.level()).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void singleStrongSignIsMediumNotHigh() { // SRS US-02 AC3, ARCHITECTURE §4.3 example 4
        RiskAssessment result = scorer.score(List.of(finding(Category.LINK, Weight.STRONG)));

        assertThat(result.score()).isEqualTo(35);
        assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void onlyStrongestFindingPerCategoryCounts() {
        RiskAssessment result = scorer.score(List.of(
                finding(Category.URGENCY, Weight.MODERATE),
                finding(Category.URGENCY, Weight.MODERATE),
                finding(Category.URGENCY, Weight.WEAK),
                finding(Category.URGENCY, Weight.STRONG)));

        assertThat(result.score()).isEqualTo(35);
        assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void scoreAtThresholdReachesThatLevel() {
        // 10 + 10 + 5 = 25 = medium threshold
        RiskAssessment result = scorer.score(List.of(
                finding(Category.INVESTMENT, Weight.WEAK),
                finding(Category.URGENCY, Weight.WEAK),
                finding(Category.ACCOUNT, Weight.INFO)));

        assertThat(result.score()).isEqualTo(25);
        assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void returnsAllFindingsStrongestFirst() {
        RiskAssessment result = scorer.score(List.of(
                finding(Category.URGENCY, Weight.WEAK),
                finding(Category.LINK, Weight.STRONG),
                finding(Category.URGENCY, Weight.MODERATE)));

        assertThat(result.findings())
                .extracting(Finding::weight)
                .containsExactly(Weight.STRONG, Weight.MODERATE, Weight.WEAK);
    }

    @Test
    void highNeedsEnoughCategoriesEvenIfThresholdIsReached() {
        // With a low high-threshold, one category alone could reach it; the guard caps it.
        var policy = new ScoringPolicy(ScoringPolicy.defaults().points(), 10, 30, 2, Set.of());
        RiskAssessment result = new RiskScorer(policy).score(List.of(finding(Category.LINK, Weight.STRONG)));

        assertThat(result.score()).isEqualTo(35);
        assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void accountOnlyIsCappedAtMedium() { // SRS US-06 AC4
        var policy = new ScoringPolicy(ScoringPolicy.defaults().points(), 2, 4, 1, Set.of(Category.ACCOUNT));
        RiskAssessment result = new RiskScorer(policy).score(List.of(finding(Category.ACCOUNT, Weight.INFO)));

        assertThat(result.level()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void sameFindingsAlwaysGiveSameResult() { // SRS US-02 AC5
        List<Finding> findings = List.of(
                finding(Category.INVESTMENT, Weight.STRONG),
                finding(Category.URGENCY, Weight.MODERATE));

        assertThat(scorer.score(findings)).isEqualTo(scorer.score(findings));
    }

    @Test
    void policyRejectsMissingTierAndBadThresholds() {
        Set<Category> none = Set.of();
        assertThatIllegalArgumentException().isThrownBy(
                () -> new ScoringPolicy(Map.of(Weight.STRONG, 35), 25, 60, 2, none));
        Map<Weight, Integer> points = ScoringPolicy.defaults().points();
        assertThatIllegalArgumentException().isThrownBy(() -> new ScoringPolicy(points, 60, 25, 2, none));
        assertThatIllegalArgumentException().isThrownBy(() -> new ScoringPolicy(points, 25, 60, 0, none));
    }
}
