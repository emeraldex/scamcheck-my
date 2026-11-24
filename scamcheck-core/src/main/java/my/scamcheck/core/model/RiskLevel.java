package my.scamcheck.core.model;

/**
 * Overall rating. {@link #LOW} means "no common warning signs found",
 * never "safe" (SRS US-02 AC2).
 */
public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}
