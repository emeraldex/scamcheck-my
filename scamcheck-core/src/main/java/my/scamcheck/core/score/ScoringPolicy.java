package my.scamcheck.core.score;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import my.scamcheck.core.model.Category;
import my.scamcheck.core.model.Weight;

/**
 * All tunable scoring numbers in one place (ADR-005). Phase 5 adjusts these
 * against the sample sets without touching any rule.
 *
 * @param points                   points per weight tier; must cover every tier
 * @param mediumThreshold          lowest score rated MEDIUM
 * @param highThreshold            lowest score rated HIGH
 * @param minCategoriesForHigh     HIGH needs findings in at least this many categories
 * @param cappedAtMediumWhenAlone  categories that can't reach HIGH on their own
 */
public record ScoringPolicy(
        Map<Weight, Integer> points,
        int mediumThreshold,
        int highThreshold,
        int minCategoriesForHigh,
        Set<Category> cappedAtMediumWhenAlone) {

    public ScoringPolicy {
        Objects.requireNonNull(points, "points");
        for (Weight weight : Weight.values()) {
            Integer value = points.get(weight);
            if (value == null || value < 0) {
                throw new IllegalArgumentException("points must give a non-negative value for " + weight);
            }
        }
        if (mediumThreshold <= 0 || highThreshold <= mediumThreshold) {
            throw new IllegalArgumentException("need 0 < mediumThreshold < highThreshold");
        }
        if (minCategoriesForHigh < 1) {
            throw new IllegalArgumentException("minCategoriesForHigh must be at least 1");
        }
        points = Map.copyOf(new EnumMap<>(points));
        cappedAtMediumWhenAlone = Set.copyOf(cappedAtMediumWhenAlone);
    }

    /** The v1 starting values from ADR-005. */
    public static ScoringPolicy defaults() {
        return new ScoringPolicy(
                Map.of(Weight.INFO, 5, Weight.WEAK, 10, Weight.MODERATE, 20, Weight.STRONG, 35),
                25,
                60,
                2,
                Set.of(Category.ACCOUNT));
    }

    public int pointsFor(Weight weight) {
        return points.get(weight);
    }
}
