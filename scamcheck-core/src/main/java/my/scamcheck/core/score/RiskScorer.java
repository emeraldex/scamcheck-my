package my.scamcheck.core.score;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.Finding;
import my.scamcheck.core.model.RiskAssessment;
import my.scamcheck.core.model.RiskLevel;

/**
 * Turns findings into a risk level using category-capped weighted scoring (ADR-005):
 * <ol>
 *   <li>per category, only the strongest finding counts;</li>
 *   <li>score = sum of those per-category maximums;</li>
 *   <li>level from the policy's thresholds;</li>
 *   <li>guards: HIGH needs enough distinct categories, and some categories
 *       (e.g. ACCOUNT) can't reach HIGH on their own.</li>
 * </ol>
 */
public class RiskScorer {

    private static final Comparator<Finding> STRONGEST_FIRST =
            Comparator.comparing(Finding::weight).reversed();

    private final ScoringPolicy policy;

    public RiskScorer(ScoringPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public RiskAssessment score(List<Finding> findings) {
        Map<Category, Integer> strongestPerCategory = new EnumMap<>(Category.class);
        for (Finding finding : findings) {
            strongestPerCategory.merge(finding.category(), policy.pointsFor(finding.weight()), Math::max);
        }

        int score = strongestPerCategory.values().stream().mapToInt(Integer::intValue).sum();
        RiskLevel level = applyGuards(levelFor(score), strongestPerCategory);
        List<Finding> sorted = findings.stream().sorted(STRONGEST_FIRST).toList();

        return new RiskAssessment(level, score, sorted);
    }

    private RiskLevel levelFor(int score) {
        if (score >= policy.highThreshold()) {
            return RiskLevel.HIGH;
        }
        if (score >= policy.mediumThreshold()) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private RiskLevel applyGuards(RiskLevel level, Map<Category, Integer> strongestPerCategory) {
        if (level != RiskLevel.HIGH) {
            return level;
        }
        boolean tooFewCategories = strongestPerCategory.size() < policy.minCategoriesForHigh();
        boolean onlyCappedCategories = policy.cappedAtMediumWhenAlone().containsAll(strongestPerCategory.keySet());
        return tooFewCategories || onlyCappedCategories ? RiskLevel.MEDIUM : RiskLevel.HIGH;
    }
}
