package my.scamcheck.core.model;

/**
 * How strong a warning sign is, declared weakest first so that the natural
 * enum order is also the strength order. Points per tier live in
 * {@link my.scamcheck.core.score.ScoringPolicy}, not here, so they can be tuned
 * without touching rules (ADR-005).
 */
public enum Weight {
    INFO,
    WEAK,
    MODERATE,
    STRONG
}
