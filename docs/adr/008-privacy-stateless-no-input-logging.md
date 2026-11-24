# ADR-008: Stateless service with no input logging

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

Users will paste messages containing names, phone numbers and bank account numbers (R5). The SRS requires that input is never stored or logged (US-10, NFR-04). Privacy leaks usually happen by accident: a debug log line, an exception message that echoes the request, or a request-logging filter.

## Decision

1. **No persistence layer at all.** No database, cache, file writing or session state. No persistence dependencies in either module.
2. **No input in logs.** Rules and the analyser don't log. The controller logs only metadata (input length, risk level, rule IDs that fired, duration). There are no request-body logging filters.
3. **Errors don't echo input.** Validation errors say *what* is wrong ("text must be 5,000 characters or fewer"), never the submitted value. `server.error.include-message` and `include-binding-errors` stay at their safe defaults.
4. **A test proves it.** An API-level test sends input containing a unique canary string through both the success path and the error paths, captures all log output (Spring Boot's `OutputCaptureExtension`), and fails if the canary appears.
5. **HTTPS only** on the live host; the UI states "We don't store what you paste" (US-10 AC3).

## Consequences

- Simple to explain and verify; nothing to secure at rest, no data-retention policy needed.
- No analytics on real inputs. Tuning relies on the sample sets (ADR-007), and the "missed scam" report path in Phase 7 must be an explicit, user-initiated action with its own privacy review.
- Debugging production issues is harder, since inputs can't be inspected after the fact. Acceptable for a stateless demo.

## Alternatives considered

- **Store anonymised inputs for tuning:** useful data, but reliable anonymisation of free text is hard, and it contradicts the plan's non-goal "no storing of user-submitted content".
- **Mask personal data in logs:** masking free text reliably is hard and error-prone; not logging at all is simpler and safer.
