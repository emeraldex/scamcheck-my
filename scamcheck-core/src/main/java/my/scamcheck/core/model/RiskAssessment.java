package my.scamcheck.core.model;

import java.util.List;
import java.util.Objects;

/**
 * The engine's result.
 *
 * @param level    overall risk level
 * @param score    sum of the strongest finding per category (ADR-005)
 * @param findings every finding, strongest first
 */
public record RiskAssessment(RiskLevel level, int score, List<Finding> findings) {

    public RiskAssessment {
        Objects.requireNonNull(level, "level");
        if (score < 0) {
            throw new IllegalArgumentException("score must not be negative");
        }
        findings = List.copyOf(findings);
    }
}
