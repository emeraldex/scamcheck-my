# ScamCheck MY — Test Plan (v1)

| | |
|---|---|
| **Status** | Draft v1.0 — for review |
| **Author** | Azlan |
| **Last updated** | 2026-11-24 |
| **Related** | [SRS](SRS.md) · [Architecture](ARCHITECTURE.md) · [ADRs](adr/) |

This plan says how version 1 is tested: what each level of testing covers, which goals and requirements it proves, and what is still checked by hand.

---

## 1. Scope

**In scope:** the rules engine (`scamcheck-core`), the REST API and the web page (`scamcheck-api`), privacy and security behaviour, and the accuracy goals G1/G2.

**Out of scope for v1:** load testing beyond a performance smoke test, cross-browser automation, penetration testing, and accessibility audits with assistive technology. These are listed under §8 as future work.

## 2. Test levels

All automated tests run with `./mvnw verify`, locally and in CI on every pull request ([ci.yml](../.github/workflows/ci.yml)).

| Level | What it checks | Where | Examples |
|---|---|---|---|
| **Unit** | One class in isolation: normaliser, extractors, loaders, each rule, scorer | `scamcheck-core/src/test` | `TextNormalizerTest`, `UrlExtractorTest`, `NumberExtractorTest`, `KeywordRuleTest`, `RiskScorerTest` |
| **Rule acceptance** | The shipped data files against SRS acceptance criteria, named `usNN_acN_…` | `scamcheck-core/src/test` | `KeywordRulesTest`, `LinkRulesTest`, `PhoneFormatRuleTest`, `AccountNumberRuleTest` |
| **Engine scenarios** | Whole messages through `ScamAnalyzer`, including the worked examples in ARCHITECTURE §4.3 | `ScamAnalyzerTest` | Investment scam with lookalike link → HIGH 90 |
| **Sample-set accuracy** | G1/G2 over 64 tuning samples, and 40 held-out samples (§4) | `SampleSetAccuracyTest` | ≥ 90% scams MEDIUM+, ≤ 10% legitimate HIGH |
| **Performance smoke** | Largest allowed input analysed well within budget | `PerformanceSmokeTest` | Engine p95 ≤ 50 ms for 5,000 chars |
| **API integration** | HTTP contract, localisation and errors via MockMvc with the real Spring context | `scamcheck-api/src/test` | `AnalyzeControllerTest` |
| **Privacy** | No input in logs at DEBUG, on success and error paths | `NoInputLoggingTest` | Canary string never appears in output |
| **Localisation completeness** | Every message key exists in EN and BM, for API and UI | `MessageBundleCompletenessTest`, `StaticPageGuardTest` | New rule without BM text fails the build |
| **Static page guards** | CSP and XSS rules for the page | `StaticPageGuardTest` | No `innerHTML`, no inline scripts, no `console.` |
| **Web and headers** | Page and assets served, security headers on every response | `WebPageTest` | `Content-Security-Policy` present on page and API |
| **Actuator exposure** | Health check works; no other actuator endpoint is reachable | `ActuatorExposureTest` | `/actuator/env` → 404 |
| **Container smoke** | The Docker image starts in 512 MB, passes its health check and answers correctly | CI job `Docker image` | Known scam → HIGH; not running as root |
| **Manual** | Real browser behaviour (§6) | Checklist below | 360 px layout, language toggle |

## 3. Requirements coverage

| Requirement | Verified by |
|---|---|
| **G1** ≥ 90% of scam samples rated MEDIUM or HIGH (NFR-02) | `SampleSetAccuracyTest.g1_…` |
| **G2** ≤ 10% of legitimate samples rated HIGH (NFR-03) | `SampleSetAccuracyTest.g2_…` |
| **G3** every finding explained | `AnalyzeControllerTest`, `MessageBundleCompletenessTest` |
| **G4** official help on every result | `AnalyzeControllerTest.lowResultNeverSaysSafeAndStillShowsOfficialHelp` |
| **G5** EN + BM | `MessageBundleCompletenessTest`, `StaticPageGuardTest`, keyword tests in both languages |
| **G6 / NFR-01** p95 < 500 ms | `PerformanceSmokeTest` (engine), smoke test on the live URL (M6) |
| **G7** CI green, coverage met | CI workflow, JaCoCo gate |
| **NFR-04** no storage or logging of input | `NoInputLoggingTest`, redacted `toString` tests, ADR-008 |
| **NFR-05** security | `WebPageTest`, `StaticPageGuardTest`, `AnalyzeControllerTest` (size limit, errors never echo input), `ActuatorExposureTest` |
| **NFR-08** runs as one Docker container | CI job `Docker image` (build, health check, smoke test) |
| **NFR-06** new rule without touching others | `ScamAnalyzerTest.Wiring` (hand-made rule plugs in) |
| **NFR-07** every rule has positive and negative tests | Rule acceptance tests |
| **SRS stories US-01 to US-14** | Each test names the story and criterion it covers; see PR descriptions for the mapping |

## 4. Sample sets

```
scamcheck-core/src/test/resources/samples/
├── scam/      32 messages: investment, police/agency impersonation, bank phishing, TAC/OTP,
│              parcel, prize, job, loan, romance, shopping, government aid, telco, family
├── legit/     32 messages: bank TAC notices, card alerts, deliveries, receipts, promotions,
│              government reminders, work, family and casual chat (incl. "untung")
└── heldout/   40 messages (20 scam, 20 legit), written without seeing the rules. Never tuned on.
```

- One message per file. Header lines start with `#` and record kind, type and language.
- **No real personal data.** Names are fictional; numbers, accounts and links are made up (ADR-007).
- Scam samples are paraphrased from common patterns in public scam-awareness material, never copied from private messages.
- The test prints a per-sample report (level, score, rules fired), so tuning changes can be compared run to run.

### Results (2026-11-24)

| | Before tuning | After tuning | Target |
|---|---|---|---|
| Scam samples rated MEDIUM or HIGH | 26/32 (81%) | **32/32 (100%)** | ≥ 90% |
| Legitimate samples rated HIGH | 0/32 (0%) | **0/32 (0%)** | ≤ 10% |
| Legitimate samples rated MEDIUM | 0/32 | 0/32 | (no target) |

**What was tuned:** missed scams were prize draws, fake jobs, romance and "send me the OTP" messages. Three keyword categories were added (`keyword.prize`, `keyword.job-offer`, `keyword.money-request`) and the credential list now covers card and bank details. The scoring model (ADR-005) was **not** changed.

**Limitation:** the tuning samples are also what the numbers above measure, so 100% is optimistic. The held-out set below shows by how much.

### Held-out results (2026-11-24)

A separate writer who had never seen the rule files or the tuning samples wrote 20 scam and 20 legitimate messages: Malaysian SMS and WhatsApp style, in BM, English and mixed, with informal spellings. They were added and measured **without changing any rule**.

| | Tuning set | **Held-out set** | Target |
|---|---|---|---|
| Scam samples rated MEDIUM or HIGH | 32/32 (100%) | **5/20 (25%)** | ≥ 90% |
| Legitimate samples rated HIGH | 0/32 (0%) | **0/20 (0%)** | ≤ 10% |
| Legitimate samples rated MEDIUM | 0/32 | 0/20 | (no target) |

**G2 holds on unseen messages. G1 does not:** the keyword rules were fitted to the tuning samples and generalise poorly. `SampleSetAccuracyTest` enforces G2 on the held-out set but only reports its scam detection. Enforcing G1 there would push us to tune to it, and then it would no longer be held out.

Why the 15 held-out scams were missed:

| Cause | Examples (held-out files) |
|---|---|
| **No matching vocabulary.** The scam's wording isn't in any list | "suspended", "deactivated", "customs clearance fee", "forward me the 6-digit code", "training kit", "deposit to lock the room", "download the APK", "2–3% daily" (h02, h07, h11, h14, h16–h19) |
| **One signal alone stays LOW by design.** A single MODERATE finding is 20 points, under the MEDIUM threshold of 25 (ADR-005) | "tolong bank in" from a "new number" (h12), "pulangan 8% tiap minggu" (h05) |
| **Several link signs count once.** An unusual ending plus no HTTPS is still one LINK finding, worth 10 | `http://m2u-secure-verify.xyz` (h02), `.top`/`.cc`/`.site` links (h10, h13, h20) |
| **Lookalikes of non-bank brands** aren't checked. The lookalike rule only knows banks, and "m2u" isn't one of its brand tokens | Pos Laju, J&T, Touch 'n Go, Shopee, gov.my lookalikes (h01, h09, h10, h15, h20) |

These point to rule and scoring work for a future release (§8). Once the held-out messages are used to tune, they become tuning samples, and a new held-out set must be written to measure the result.

### Adding a sample

1. Add `scam-NNN-<type>-<lang>.txt` or `legit-NNN-<type>-<lang>.txt` with the three header lines.
2. Use only fictional names and made-up numbers and links.
3. Run `./mvnw -pl scamcheck-core test -Dtest=SampleSetAccuracyTest` and read the report.

## 5. Coverage target

JaCoCo runs in every build. **The build fails below 90% line coverage in either module.**

| Module | Line | Branch | Baseline date |
|---|---|---|---|
| scamcheck-core | 94% | 87% | 2026-11-24 |
| scamcheck-api | 97% | 95% | 2026-11-24 |

Reports: `<module>/target/site/jacoco/index.html`, and the `reports` artifact on each CI run.

## 6. Manual test checklist

Run against the packaged jar (`java -jar scamcheck-api/target/scamcheck.jar`) before each release, and on the live URL after deploying.

- [ ] Page loads at 360 px wide with no horizontal scrolling; text ≥ 16 px; Check button ≥ 44 px tall
- [ ] Light and dark mode both readable; risk level shown with text and icon, not colour alone
- [ ] Try an example → HIGH RISK with explained findings; Call 997 shown above Semak Mule
- [ ] Toggle EN ↔ BM with a result showing → explanations change language
- [ ] Empty Check → inline error; over 5,000 characters → inline error and red counter
- [ ] Check another → page cleared, focus back in the text box
- [ ] Language choice survives a reload
- [ ] Browser console shows no errors and no Content-Security-Policy violations
- [ ] `tel:997` opens the phone dialler on a phone; Semak Mule opens in a new tab
- [ ] Keyboard only: Tab reaches every control; Ctrl+Enter submits

## 7. Security checks

| Check | How |
|---|---|
| Input size limit | `AnalyzeControllerTest.tooLongTextIsRejectedWithoutEchoingIt` |
| No input echoed in errors or logs | `AnalyzeControllerTest`, `NoInputLoggingTest`, ADR-010 |
| XSS: input never rendered as HTML | `StaticPageGuardTest`, strict CSP (`WebPageTest`) |
| Clickjacking | `X-Frame-Options: DENY`, CSP `frame-ancestors 'none'` |
| Links never fetched | URL extraction only parses text (SRS US-04 AC8) |
| Vulnerable dependencies | Dependabot weekly PRs; each must pass CI |
| Secrets | None in the repository; the app needs no secrets |
| Management endpoints | Only `/actuator/health`, without details (`ActuatorExposureTest`, ADR-011) |
| Container | Runs as a non-root user on a JRE-only image (CI `Docker image` job) |

## 8. Future work

- Close the held-out gaps (§4): broader vocabulary, lookalikes of non-bank brands, and a review of single-signal scoring. Then move the held-out set into the tuning set and write a fresh held-out set to measure the change.
- Browser end-to-end tests (e.g. Playwright) for the manual checklist
- Accessibility audit with a screen reader and an automated contrast check
- Load test on the live host to confirm NFR-01 including network time
- Mutation testing (e.g. PIT) on `scamcheck-core` to measure test strength beyond coverage
