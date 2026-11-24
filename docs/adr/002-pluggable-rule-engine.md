# ADR-002: Pluggable rule engine with up-front extraction

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

The SRS needs about a dozen checks across four kinds of input (keywords, links, phone numbers, account numbers). Scam wording changes often (R9), so adding or changing a check must be cheap and must not risk breaking other checks (NFR-06). Every check needs its own unit tests (NFR-07), and every result must be explainable rule by rule (G3).

Several checks need the same parsing. For example, all five link checks need the URLs in the text, and the account check must ignore digits already identified as a phone number (SRS US-06 AC3).

## Decision

1. **A `Rule` interface** with three methods: `id()`, `category()` and `evaluate(ExtractedInput) → List<Finding>`. Rules are stateless, thread-safe, and do no I/O.
2. **Extraction runs once, before any rule.** An `InputExtractor` normalises the text and pulls out URL, phone and account candidates into an immutable `ExtractedInput` record. Phone numbers are claimed before account numbers, so the same digits are never reported twice.
3. **Rules produce findings, not scores.** A `Finding` carries the rule ID, category, a `Weight` tier, the matched text, and a message key. A separate `RiskScorer` turns findings into a level ([ADR-005](005-scoring-model.md)).
4. **`ScamAnalyzer` is the single entry point.** It receives its list of rules through its constructor. It has no service locator, classpath scanning or annotations; the adapter module does the wiring.
5. **Data-driven keyword rules.** The five keyword categories share one `KeywordRule` class, each instance loaded from its own data file ([ADR-007](007-rule-data-and-sample-sets.md)).

## Consequences

- A new check means one class, its test, and one line of wiring. The engine and other rules are untouched (Open/Closed Principle).
- Each rule can be unit-tested by building an `ExtractedInput` by hand, with no Spring and no parsing.
- Extraction bugs affect several rules at once, so the extractor needs its own thorough tests.
- Rules can't see each other's findings. Combinations ("urgency + link") are handled by the scorer, not inside rules. That keeps rules simple but means any "combo" logic lives in one place: the scorer.

## Alternatives considered

- **A rules-engine library (Drools, Easy Rules):** powerful, but heavy for about 12 rules. It hides simple logic behind a DSL and makes the core depend on a framework.
- **Each rule parses the raw text itself:** simpler interface, but duplicated parsing and no clean way to stop phone digits being reported as accounts.
- **One big `if/else` analyser class:** fastest to write, hardest to test and extend; contradicts NFR-06.
- **Rules return a score directly:** couples every rule to the scoring model; changing thresholds would mean editing rules.
