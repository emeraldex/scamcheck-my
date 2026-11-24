# ADR-006: Static HTML/JS page instead of Thymeleaf

- **Status:** Proposed
- **Date:** 2026-11-24
- **Resolves:** SRS open question Q4, Project Plan §5 [DECIDE]

## Context

Version 1 needs one simple page: a text box, a Check button, an EN/BM toggle, and a results area (see [WIREFRAME.md](../WIREFRAME.md)). It must work well on phones (US-13) and must never render user input as HTML (NFR-05).

The Project Plan lists two options: server-rendered Thymeleaf (everything in Java) or a small static page that calls the JSON API (shows the API off more clearly).

## Decision

Build the UI as **one static page with plain HTML, CSS and vanilla JavaScript**, served by Spring Boot from `scamcheck-api/src/main/resources/static/`. The page calls `POST /api/v1/analyze` with `fetch` and renders the JSON response.

- No front-end framework, no Node, no build step.
- UI text (labels, buttons, notices) lives in `static/i18n/en.json` and `ms.json`. Finding explanations and tips come from the API, already localised ([ADR-009](009-localisation-message-keys.md)).
- All dynamic content is inserted with `textContent` / `createElement`, never `innerHTML`. A strict Content-Security-Policy (`default-src 'self'`) blocks inline scripts.

## Consequences

- The page uses exactly the same API contract as any other client, so the API is proven by real use.
- Reviewers can read the whole front end in a few minutes; there's no toolchain to install.
- The UI could later move to a CDN or separate host without changing the backend.
- Two places hold translations: JSON files for UI text, properties files for API messages. A test should check both language files have the same keys.
- No server-side rendering, so the page needs JavaScript. Acceptable for the target users' smartphones.

## Alternatives considered

- **Thymeleaf:** all-Java and works without JavaScript, but hides the API behind a form post, and the API would need separate proof (Swagger UI only).
- **React/Vue/Svelte SPA:** modern and familiar to front-end reviewers, but a Node toolchain and build pipeline for one page is scope creep (R3) for a backend-focused portfolio.
- **HTMX:** small and elegant, but expects server-rendered HTML fragments, which brings back the Thymeleaf trade-off.
