# ScamCheck MY — Software Requirements Specification (v1)

| | |
|---|---|
| **Status** | Draft v1.0 — for review |
| **Author** | Azlan |
| **Last updated** | 2026-11-24 |
| **Related** | [Project Plan](PROJECT_PLAN(rev'd).md) (goals G1–G7, risks R1–R9) |

> This document says **what** version 1 must do, not **how**. Design choices (framework, rule interface, scoring weights, UI technology) belong in the Phase 3 ADRs.
> Keywords **MUST**, **SHOULD** and **MAY** are used as in RFC 2119.

---

## 1. Introduction

### 1.1 Purpose
ScamCheck MY gives Malaysians a quick, explainable second opinion on a suspicious message, link, phone number or bank account number. It points them to official channels (PDRM Semak Mule, NSRC 997) for verification and reporting.

### 1.2 Scope
Version 1 is a stateless web application: one REST API and one web page. It analyses pasted text with hand-written rules and returns a risk level, the warning signs it found, and links to official help. The non-goals in Project Plan §4 apply in full: no scraping, no storage of input, no accounts, no ML/LLM, no third-party reputation lookups.

### 1.3 Definitions

| Term | Meaning |
|---|---|
| **Input** | The text a user pastes into the single text box. It can contain a message, URLs, phone numbers and account numbers, in any mix. |
| **Rule** | One self-contained check that looks at the input and either produces a finding or not. |
| **Finding** | The result of a rule firing: the rule ID, a severity weight, the part of the input that matched, and a plain-language explanation. |
| **Risk level** | The overall rating: **Low**, **Medium** or **High**, worked out from all findings. |
| **Low** | "No common warning signs found." It never means "safe" (R1). |
| **Official help** | Links to Semak Mule (https://semakmule.rmp.gov.my), the NSRC 997 hotline, and a short "what to do now" tip. |
| **BM** | Bahasa Melayu (Malay). |

---

## 2. Overall description

### 2.1 Product perspective
A new, standalone system. It has no database and calls no external services at analysis time. Government sites are only linked to, never queried.

### 2.2 Users
See Project Plan §2. In short: everyday smartphone users (primary), older and less tech-savvy users, family members helping them, and technical reviewers.

### 2.3 Operating environment
- Any current mobile or desktop browser (latest two versions of Chrome, Safari, Firefox, Edge).
- Server: a JVM (Java 21) application in a container on a free hosting tier.

### 2.4 Constraints
- C1: Java 21, multi-module Maven build.
- C2: Free hosting only. Cold starts are acceptable.
- C3: Keyword and pattern lists are curated from public scam-awareness material.
- C4: No PDRM, NSRC or bank logos or branding (R6).

### 2.5 Assumptions
- A1: Users can copy and paste text on their device.
- A2: Scam wording changes over time, so rules must be easy to add or change (R9).

---

## 3. User stories and acceptance criteria

Stories use the format *As a … I want … so that …*. Acceptance criteria use *Given / When / Then* and become test cases in Phase 4. IDs match the branch names in the plan (e.g. `feature/US-03-keyword-rules`).

**Priority:** **M** = Must for v1, **S** = Should, **C** = Could (cut first if time runs short).

### Story map

| ID | Story | Priority | Goals |
|---|---|---|---|
| US-01 | Paste input and get an analysis | M | G3, G6 |
| US-02 | See an overall risk level | M | G1, G2 |
| US-03 | Detect scam keywords and phrases (EN + BM) | M | G1, G5 |
| US-04 | Detect suspicious links | M | G1 |
| US-05 | Check phone numbers | M | G1 |
| US-06 | Recognise bank account numbers | M | G4 |
| US-07 | Understand why each warning was raised | M | G3 |
| US-08 | Get official places to check or report | M | G4 |
| US-09 | Use the app in Malay or English | M | G5 |
| US-10 | Trust that my input is not kept | M | — (R5) |
| US-11 | Call the analysis as a JSON API | M | G7 |
| US-12 | See that the app is unofficial advice | M | — (R1, R6) |
| US-13 | Use it comfortably on a phone | S | — |
| US-14 | Try a sample scam message | C | — |

---

### US-01 — Paste input and get an analysis
*As a smartphone user, I want to paste a suspicious message into one box and get a result, so that I don't have to work out what kind of thing I'm checking.*

- **AC1** Given the text box contains a message, when I press **Check**, then I see a result on the same page.
- **AC2** Given the input contains a URL, a phone number and an account number together, when it is analysed, then rules for all three kinds are applied.
- **AC3** Given the input is empty or only whitespace, when I press **Check**, then no analysis runs and I see "Please paste something to check."
- **AC4** Given the input is longer than 5,000 characters, when I press **Check**, then it is rejected with a message stating the limit.

### US-02 — See an overall risk level
*As a user, I want one clear rating, so that I know at a glance how worried to be.*

- **AC1** Every result shows exactly one risk level: Low, Medium or High.
- **AC2** Given no rule fires, then the level is **Low** and the text reads "No common warning signs found", never "safe".
- **AC3** Given only one low-weight finding (for example the word "untung" on its own), then the level is **not High** (R2: no one-hit-High).
- **AC4** Given several independent strong signs (for example guaranteed returns + urgency + a lookalike bank link), then the level is **High**.
- **AC5** The same input always gives the same level (deterministic).

### US-03 — Detect scam keywords and phrases (EN + BM)
*As a user, I want the app to spot the wording scammers use, so that I notice pressure tactics I might miss.*

Categories: investment/guaranteed returns, urgency, account threats, authority impersonation, TAC/OTP requests.

- **AC1** Given "Pelaburan dijamin untung 30% sebulan", then an *investment / guaranteed return* finding is raised.
- **AC2** Given "Your account has been suspended, act now", then *account threat* and *urgency* findings are raised.
- **AC3** Given "Akaun anda dibekukan. Sila hubungi segera", then *account threat* and *urgency* findings are raised.
- **AC4** Given "Please share the TAC/OTP you received", then a *TAC/OTP request* finding is raised.
- **AC5** Matching ignores case and extra spaces ("SEGERA", "akaun  dibekukan").
- **AC6** Matching respects word boundaries: "untung" fires, but "beruntung" does not fire the *investment* rule. Each such exception is backed by a test.
- **AC7** Common BM short forms SHOULD be recognised where they appear in the curated list (e.g. "utk", "x" for "tidak") (R7).

### US-04 — Detect suspicious links
*As a user, I want risky links flagged, so that I don't open a fake login page.*

- **AC1** Given `https://maybank2u-secure.xyz/login`, then a *lookalike bank domain* finding is raised.
- **AC2** Given the genuine domain `https://www.maybank2u.com.my`, then no lookalike finding is raised.
- **AC3** Given a known URL shortener (e.g. `bit.ly/abc`), then a *URL shortener* finding is raised that explains the real destination is hidden.
- **AC4** Given a top-level domain on the curated "unusual" list (e.g. `.xyz`, `.top`), then an *unusual domain ending* finding is raised.
- **AC5** Given a URL whose host is a raw IP address, then a *raw IP link* finding is raised.
- **AC6** Given a plain `http://` link, then a *not secure link* finding is raised with low weight.
- **AC7** URLs without a scheme (e.g. `maybank2u-secure.xyz`) are still detected.
- **AC8** The app never fetches or opens any link it analyses.

### US-05 — Check phone numbers
*As a user, I want to know if a phone number looks odd, so that I'm careful before calling back.*

- **AC1** Valid Malaysian mobile formats are recognised without a warning: `012-345 6789`, `+60123456789`, `011-2345 6789`.
- **AC2** Given a number that looks local but has an invalid length or prefix, then an *invalid Malaysian number* finding is raised.
- **AC3** Given an international number presented as a local contact (non-+60 country code), then an *international number* finding is raised.
- **AC4** Every phone finding reminds the user that they can check the number on Semak Mule.

### US-06 — Recognise bank account numbers
*As a user, I want account numbers spotted, so that I'm reminded to check them before transferring.*

- **AC1** Given a digit sequence in a typical Malaysian account-number format, then an *account number found* finding is raised.
- **AC2** That finding states that the app **cannot** tell whether the account is a mule account, and links to Semak Mule.
- **AC3** Digits already identified as a phone number are not also reported as an account number.
- **AC4** Recognising an account number alone does not raise the level above Medium.

### US-07 — Understand why each warning was raised
*As a user, I want each warning explained in plain words, so that I learn to spot scams myself.*

- **AC1** Every finding shows the rule's name, the matched text, and a one- or two-sentence explanation.
- **AC2** Explanations contain no technical jargon (no "regex", "TLD", "heuristic").
- **AC3** Findings are listed strongest first.

### US-08 — Get official places to check or report
*As a user, I want to know where to go next, so that I can verify or report the scam.*

- **AC1** Every result, including Low, shows links to Semak Mule and NSRC 997.
- **AC2** On a phone, 997 is a `tel:` link.
- **AC3** Every result shows a short "what to do now" tip matching the risk level.
- **AC4** Official links open the official site in a new tab. No official content is copied or embedded.

### US-09 — Use the app in Malay or English
*As an older user, I want the app in my language, so that I understand the advice.*

- **AC1** The UI has an EN/BM toggle. The default follows the browser language, falling back to BM.
- **AC2** All UI text, finding explanations and tips exist in both languages.
- **AC3** Keyword rules work on both EN and BM input, whichever UI language is chosen.
- **AC4** The API accepts a language parameter (`en` or `ms`) for explanation text.

### US-10 — Trust that my input is not kept
*As a user pasting personal details, I want my input forgotten, so that my data isn't exposed.*

- **AC1** Input is never written to a database, file or cache.
- **AC2** Input and matched text never appear in application logs, at any log level.
- **AC3** The UI states "We don't store what you paste."
- **AC4** The live site is served over HTTPS only.

### US-11 — Call the analysis as a JSON API
*As a technical reviewer, I want a documented API, so that I can see the backend design and integrate with it.*

- **AC1** `POST` an input (and optional language) as JSON; the response contains the risk level, a score, a list of findings and the official help links.
- **AC2** Invalid requests (empty, too long, malformed JSON) return HTTP 400 with a consistent error body.
- **AC3** The API is documented (OpenAPI) and the docs are reachable from the README.
- **AC4** Exact paths and field names are defined in the Phase 3 API spec.

### US-12 — See that the app is unofficial advice
*As any user, I want to know this is not an official service, so that I treat the result as a hint, not a verdict.*

- **AC1** Every page shows: "Not affiliated with PDRM, NSRC or any bank. This tool gives advice only."
- **AC2** No government or bank logos are used anywhere.

### US-13 — Use it comfortably on a phone *(Should)*
- **AC1** The page works at 360 px wide without horizontal scrolling.
- **AC2** Base text size is at least 16 px; the Check button is at least 44 × 44 px.
- **AC3** The page meets WCAG 2.1 AA colour contrast, and the risk level is shown with text as well as colour.

### US-14 — Try a sample scam message *(Could)*
- **AC1** A "Try an example" button fills the box with a sample scam message in the current UI language.

---

## 4. Non-functional requirements

| ID | Category | Requirement | Verified by |
|---|---|---|---|
| NFR-01 | Performance | Analysis endpoint p95 < 500 ms for inputs ≤ 5,000 chars, excluding cold start (G6). | Load test in Phase 5 |
| NFR-02 | Accuracy | ≥ 90% of the sample scam set rated Medium or High (G1). | Sample-set test in CI |
| NFR-03 | Accuracy | ≤ 10% of the sample legitimate set rated High (G2). | Sample-set test in CI |
| NFR-04 | Privacy | No storage or logging of input (US-10). | Code review, log test |
| NFR-05 | Security | Input size limit enforced; no input is executed, fetched or rendered as HTML (XSS-safe output). | Tests, OWASP checklist |
| NFR-06 | Maintainability | A new rule can be added without changing the engine or other rules (R9). | Design review, ADR |
| NFR-07 | Maintainability | Every rule has its own unit tests covering positive and negative cases. | CI coverage report |
| NFR-08 | Portability | Runs as a single Docker container (R8). | CI build |
| NFR-09 | Availability | Best effort on a free tier; cold start noted in the README. | — |
| NFR-10 | Usability | Results readable by non-technical users in EN and BM; BM text reviewed by a native speaker (R7). | Manual review |

---

## 5. Traceability

| Goal | Covered by |
|---|---|
| G1 Spot warning signs | US-02, US-03, US-04, US-05, NFR-02 |
| G2 Avoid crying wolf | US-02 AC3, NFR-03 |
| G3 Explain every result | US-01, US-07 |
| G4 Official help | US-06, US-08 |
| G5 EN + BM | US-03, US-09 |
| G6 Fast | NFR-01 |
| G7 Professional engineering | US-11, NFR-06, NFR-07, NFR-08 |

---

## 6. Open questions (resolved in Phase 3)

- **Q1** Scoring: weighted sum with thresholds, or category-based escalation? → [ADR-005](adr/005-scoring-model.md)
- **Q2** Which Malaysian bank domains go on the genuine list for lookalike checks, and how do we keep it current? → [ADR-007](adr/007-rule-data-and-sample-sets.md)
- **Q3** Bank account formats: one generic digit-length rule, or per-bank patterns? → [ADR-007](adr/007-rule-data-and-sample-sets.md)
- **Q4** Web UI: Thymeleaf or static HTML/JS (Project Plan §5 [DECIDE]). → [ADR-006](adr/006-static-web-ui.md)
- **Q5** Sample data sets: where do the scam and legitimate samples come from, and how are they kept free of real personal data? → [ADR-007](adr/007-rule-data-and-sample-sets.md)

---

### SRS v1 sign-off checklist
- [ ] Every Must story has testable acceptance criteria
- [ ] Every goal G1–G7 traces to at least one story or NFR
- [ ] Privacy and "never say safe" rules are explicit
- [ ] Open questions are carried into Phase 3
- [ ] BM example phrases checked by a native speaker
