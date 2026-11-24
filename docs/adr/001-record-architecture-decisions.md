# ADR-001: Record architecture decisions

- **Status:** Proposed
- **Date:** 2026-11-24

## Context

ScamCheck MY is a portfolio project. Reviewers want to see not just *what* was built but *why*. Decisions made in week 2 will be forgotten by week 6 unless they're written down, and the Project Plan already defers several choices ("[DECIDE]") to Phase 3.

## Decision

Record every significant design decision as a short Markdown ADR in `docs/adr/`, numbered sequentially, using the Nygard format: Context, Decision, Consequences, Alternatives considered.

A decision is "significant" if it is hard to reverse, affects more than one module, or answers an open question in the SRS.

## Consequences

- Reviewers can follow the reasoning behind the design.
- Each ADR is reviewed in a PR like code.
- Small overhead per decision (about 15–30 minutes).
- ADRs must be kept honest: when a decision changes, write a new ADR that supersedes the old one, rather than editing history.

## Alternatives considered

- **Decisions in the README or a wiki:** easy to lose, no history, no review.
- **No written decisions:** fastest now, costly when explaining the design in an interview.
