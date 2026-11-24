# ADR-010: Explicit request validation instead of Bean Validation

- **Status:** Proposed
- **Date:** 2026-11-24
- **Amends:** [ADR-003](003-spring-boot-thin-adapter.md) (which listed Jakarta Bean Validation for request checks)

## Context

ADR-003 planned to validate `POST /api/v1/analyze` with Jakarta Bean Validation (`@NotBlank`, `@Size` and `@Valid`). While building M3, `NoInputLoggingTest` ([ADR-008](008-privacy-stateless-no-input-logging.md)) sent an over-long request containing a canary string with Spring MVC logging at DEBUG, and the canary appeared in the log:

```
DEBUG ExceptionHandlerExceptionResolver : Resolved [MethodArgumentNotValidException: ...
  Field error in object 'analyzeRequest' on field 'text': rejected value [<the user's text>] ...]
```

`MethodArgumentNotValidException` includes the **rejected value** in its message, and Spring MVC logs resolved exceptions at DEBUG. Anyone switching on DEBUG logging while troubleshooting would write users' messages to the logs, breaking SRS US-10 AC2 ("at any log level").

## Decision

Validate the request **explicitly** in `AnalyzeRequest.validate()`, called first thing in the controller. A failure throws `InvalidRequestException(field, message)`, which carries the field name and a safe message but never the value. `ApiExceptionHandler` turns it into the same RFC 9457 `validation-error` problem response as before.

The rules are unchanged: `text` must be present, not blank, and at most `AnalysisInput.MAX_LENGTH` (5,000) characters. The `spring-boot-starter-validation` dependency is removed.

## Consequences

- Input can't leak through validation errors at any log level; `NoInputLoggingTest` guards this with `org.springframework.web` at DEBUG.
- The API contract and error format are unchanged, so no client is affected.
- Two simple checks are hand-written instead of declared with annotations. If the request grows many fields, revisit this (for example, by calling a `Validator` manually and mapping violations without values).
- The length limit still has a single source of truth: `AnalysisInput.MAX_LENGTH` in the core.

## Alternatives considered

- **Keep Bean Validation and pin `ExceptionHandlerExceptionResolver` logging at INFO:** works with today's config, but the guarantee breaks silently as soon as someone sets that logger, or a parent logger with a more specific override, to DEBUG.
- **Custom `MessageInterpolator` or overriding the exception's message:** the rejected value is added by Spring's binding layer, not the constraint message, so this doesn't remove it.
- **Mask the value in a log filter:** fragile pattern matching on free text; the same reasoning as ADR-008 ("not logging at all is simpler and safer").
