# Architecture Decision Records

Each ADR records one significant decision: the context, the choice, and its consequences. Format: [Michael Nygard's template](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

**Status values:** Proposed → Accepted (or Rejected). A later ADR can mark an earlier one Superseded. Accepted ADRs are never edited except to change their status; a change of mind gets a new ADR.

| # | Title | Status | Resolves |
|---|---|---|---|
| [001](001-record-architecture-decisions.md) | Record architecture decisions | Proposed | — |
| [002](002-pluggable-rule-engine.md) | Pluggable rule engine with up-front extraction | Proposed | NFR-06, R9 |
| [003](003-spring-boot-thin-adapter.md) | Spring Boot 4 on Java 21 as a thin adapter | Proposed | Plan §6 |
| [004](004-framework-free-core-module.md) | Two modules with an enforced framework-free core | Proposed | NFR-06, NFR-07 |
| [005](005-scoring-model.md) | Category-capped weighted scoring | Proposed | SRS Q1, R2 |
| [006](006-static-web-ui.md) | Static HTML/JS page instead of Thymeleaf | Proposed | SRS Q4, Plan §5 |
| [007](007-rule-data-and-sample-sets.md) | Rule data and sample sets as versioned text files | Proposed | SRS Q2, Q3, Q5 |
| [008](008-privacy-stateless-no-input-logging.md) | Stateless service with no input logging | Proposed | US-10, R5 |
| [009](009-localisation-message-keys.md) | Message keys in core, translations in the adapter | Proposed | US-09, G5 |

## Adding an ADR

1. Copy the newest ADR, give it the next number and a short kebab-case title.
2. Set **Status: Proposed** and open a PR (`docs: add ADR-0NN <title>`).
3. On merge, change the status to **Accepted** and add it to the table above.
