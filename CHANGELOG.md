# Changelog

All notable changes to ScamCheck MY. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow [Semantic Versioning](https://semver.org/). Releasing is described in [MAINTAINING §7](docs/MAINTAINING.md#7-releasing).

## [Unreleased]

## [1.0.0] - 2026-11-24

First public release. Live at https://scamcheck-my.onrender.com.

### Added
- **Analysis engine** (`scamcheck-core`, plain Java 21 with no framework, enforced by the build):
  - Text is normalised once; links, phone numbers and account numbers are extracted up front (ADR-002, ADR-004).
  - **Keyword rules** in BM and English for eight categories: investment, urgency, account threats, impersonation, requests for codes or card details, prizes, fake jobs and money requests. Each list is a text file (ADR-007).
  - **Link rules:**
    - lookalikes of Malaysian banks, and of couriers, e-wallets, marketplaces, telcos and government sites;
    - URL shorteners, unusual domain endings, raw IP addresses, and links without HTTPS.
  - **Phone rules:** numbers that aren't valid Malaysian formats, and international numbers. An **account number** found is noted for information.
  - **Scoring:** only the strongest finding in each category counts, giving a LOW, MEDIUM or HIGH result; HIGH needs at least two categories (ADR-005).
- **REST API:** `POST /api/v1/analyze` returns the risk level, score, explained findings in EN or BM, and official help (NSRC 997, PDRM Semak Mule). Errors are RFC 9457 problem responses that never echo the input (ADR-003, ADR-009, ADR-010). The contract is in `docs/api/openapi.yaml`.
- **Web page:** static HTML and JavaScript with an EN/BM toggle, dark mode, an example message and a phone-first layout. A strict Content-Security-Policy is set, and no `innerHTML` is used (ADR-006).
- **Privacy:** nothing is stored, and input is never logged at any level. A canary test proves this (ADR-008).
- **Missed-scam reports:** each result links to a public GitHub issue form that warns users to remove personal details. The app itself sends nothing (ADR-012).
- **Operations:**
  - one Docker image (JRE 21, non-root), deployed to Render's free tier from `render.yaml`;
  - only `/actuator/health` is exposed;
  - one metadata log line per request (ADR-011).
- **Quality:**
  - CI on every PR builds and tests the code, and builds and smoke-tests the container.
  - The build fails below 90% line coverage in either module.
  - Dependabot updates Maven, Actions and Docker dependencies.
  - Tests: 231 automated tests, a 104-message tuning set, and a 50-message held-out set written without seeing the rules (`docs/TEST_PLAN.md`).
- **Docs:** project plan, SRS, architecture, ADRs 001–012, test plan, deployment guide, maintainer guide and wireframe.

### Known limitations
- **Accuracy on unseen messages:** 40% of held-out scams are rated MEDIUM or HIGH, against a 90% goal, while no normal message is rated HIGH. Subtle scams in fresh wording, and types with no signal (account takeover, APK files, QR installs), are often rated LOW. A LOW result never means "safe" (TEST_PLAN §4).
- The bank and brand domain lists, and the phone formats, are not yet verified by hand. The BM text needs a native-speaker review.
- Domain handling is not the full Public Suffix List, and there's no detection of internationalised (punycode) lookalikes.
- There's no handling of negation ("this is *not* urgent").
- The free host sleeps when idle, so the first visit can take about a minute.

[Unreleased]: https://github.com/emeraldex/scamcheck-my/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/emeraldex/scamcheck-my/releases/tag/v1.0.0
