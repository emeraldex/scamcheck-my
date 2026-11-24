# ADR-004: Two modules with an enforced framework-free core

- **Status:** Proposed
- **Date:** 2026-11-24

## Context

The valuable part of ScamCheck MY is the rule engine: extraction, rules and scoring. It should be testable in milliseconds without starting Spring (NFR-07) and reusable from other adapters later (a CLI or chat bot is on the "Later" list).

Layering rules written only in a README tend to erode: one convenient `@Component` or Spring utility import in the core, and the boundary is gone.

## Decision

1. **Two Maven modules** under `scamcheck-parent`:
   - `scamcheck-core`: plain Java 21. Domain model, extraction, rules, scoring, rule data files.
   - `scamcheck-api`: Spring Boot adapter ([ADR-003](003-spring-boot-thin-adapter.md)). Depends on `scamcheck-core`.
2. **The boundary is enforced by the build.** The core module's POM runs the Maven Enforcer `bannedDependencies` rule, which fails the build if any `org.springframework*` or `jakarta.*` artifact appears in its resolved (including transitive) dependencies. Test scope is exempt.
3. The parent POM also enforces Java ≥ 21, Maven ≥ 3.9, and `dependencyConvergence`.
4. Group ID and base package: `my.scamcheck` (Malaysia's `.my` domain).

## Consequences

- "The core is framework-free" is a property CI checks, not a convention. Breaking it fails the build in under a second (the enforcer runs in the `validate` phase).
- Core tests are fast plain JUnit 5 + AssertJ.
- The core can't use Spring conveniences (`@Value`, `Resource` loading, `MessageSource`). It loads its rule data from the classpath with the JDK, and returns message keys instead of text ([ADR-009](009-localisation-message-keys.md)).
- `spring-boot:run` with `-am` would also run on the parent and core modules. The parent sets `spring-boot.run.skip=true` and only the API module turns it back on.

## Alternatives considered

- **Single module with packages:** simpler build, but the boundary is only a convention. ArchUnit could check it, but a module boundary is clearer and checked earlier.
- **Hexagonal layout with more modules** (`domain`, `application`, `adapter-web`, …): textbook-correct, but too much structure for about 15 core classes and one adapter (YAGNI).
- **Separate `scamcheck-web` module for the page:** one static page doesn't justify its own module ([ADR-006](006-static-web-ui.md)).
