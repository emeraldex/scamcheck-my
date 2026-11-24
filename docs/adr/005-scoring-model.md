# ADR-005: Category-capped weighted scoring

- **Status:** Proposed
- **Date:** 2026-11-24
- **Resolves:** SRS open question Q1

## Context

Findings from many rules must become one risk level (SRS US-02). The model must:

- avoid **one-hit-High**: a single weak or even strong sign shouldn't produce High (R2, US-02 AC3);
- reach **High** when several independent strong signs appear together (US-02 AC4);
- not let one category dominate: a message that says "segera" five times is not five times more suspicious;
- keep account-number detection at Medium or below (US-06 AC4);
- be deterministic (US-02 AC5) and easy to explain in the UI and in an interview;
- be tunable against the sample sets (NFR-02, NFR-03) without changing rule code.

## Decision

1. Every finding has a **`Weight` tier**: `INFO` = 5, `WEAK` = 10, `MODERATE` = 20, `STRONG` = 35 points.
2. Group findings by **`Category`**. Within each category, **only the highest-weight finding counts**.
3. **Score** = sum of the per-category maximums.
4. **Level from thresholds:** score < 25 → **Low**; 25–59 → **Medium**; ≥ 60 → **High**.
5. **Guards:**
   - High requires findings in **at least two categories**; otherwise the level is capped at Medium.
   - If the only category is `ACCOUNT`, the level is capped at Medium.
6. All findings are still **returned** to the user, sorted by weight (strongest first), even those that didn't count towards the score.
7. Tier points, thresholds and guards live in one `ScoringPolicy` value object with these defaults. Phase 5 tunes them against the sample sets.

Worked examples are in [ARCHITECTURE.md §4.3](../ARCHITECTURE.md#43-scoring).

## Consequences

- Simple enough to show on screen ("2 strong signs + 1 moderate sign → High").
- Repetition within a category can't inflate the score; diversity of signs is what raises it.
- Two `STRONG` signs in different categories (35 + 35 = 70) reach High, matching the intuition "guaranteed returns + fake bank link = high risk".
- Some real scams with a single strong sign (e.g. only a lookalike link) are rated Medium, not High. This is a deliberate trade-off for fewer false alarms (R2). The UI still shows the warning and official links (R1).
- Tier points are coarse by design; if Phase 5 shows they're too coarse, a new ADR can introduce per-rule weights.

## Alternatives considered

- **Plain sum of all weights:** repeated words inflate the score; one spammy category reaches High alone.
- **Highest single finding decides:** one-hit-High; violates R2.
- **Count-based** ("3+ findings = High"): ignores strength; three weak signs would beat one strong one.
- **Probabilistic model (naive Bayes, logistic regression):** needs labelled training data and is harder to explain. Out of scope (Plan §4: rules only).
