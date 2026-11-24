# ADR-009: Message keys in core, translations in the adapter

- **Status:** Proposed
- **Date:** 2026-11-24

## Context

Every finding explanation, tip and summary must exist in English and Malay (US-09, G5). The core must stay framework-free ([ADR-004](004-framework-free-core-module.md)), so it can't use Spring's `MessageSource`. Keyword *detection* must work on both languages regardless of the UI language (US-09 AC3).

## Decision

1. **Core returns keys, not text.** Each `Finding` carries a `messageKey` (e.g. `finding.link.lookalike-bank`) and `messageArgs` (e.g. the matched domain and the bank name). The core contains no user-facing sentences.
2. **The API module translates.** `messages_en.properties` and `messages_ms.properties` in `scamcheck-api` are resolved through Spring's `MessageSource` for the requested `language` (`en` or `ms`; default `ms`).
3. **Detection is language-independent.** Keyword files contain phrases in both languages (ADR-007); all rules run on every input.
4. **A completeness test** in the API module loads every registered rule, collects the message keys they can produce, and fails if any key is missing from either bundle.
5. **UI text** (labels, buttons) lives in the static page's JSON files ([ADR-006](006-static-web-ui.md)), with a test checking both files have the same keys.

## Consequences

- Adding a language later means adding bundle files, not touching rules.
- Rule unit tests assert on keys and arguments, so they don't break when wording is improved.
- Translations live in two formats (properties and JSON). This is a small duplication accepted to keep the UI free of a build step.
- BM wording can be reviewed by a native speaker in plain text files (R7).

## Alternatives considered

- **Core returns English text; the UI translates:** the UI would need to know every possible message, and BM quality would depend on string matching.
- **`java.util.ResourceBundle` inside the core:** works without Spring, but mixes presentation into the domain module and makes the core responsible for every adapter's wording.
- **Separate rule sets per language:** duplicates logic; mixed-language messages (very common in Malaysia) would slip through.
