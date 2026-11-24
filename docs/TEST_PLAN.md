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
| **Sample-set accuracy** | G1/G2 over 104 tuning samples, and 50 held-out samples (§4) | `SampleSetAccuracyTest` | ≥ 90% scams MEDIUM+, ≤ 10% legitimate HIGH |
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
├── scam/      52 messages: investment, police/agency impersonation, bank phishing, TAC/OTP,
│              parcel, prize, job, loan, romance, shopping, government aid, telco, family,
│              crypto, rental, e-wallet
├── legit/     52 messages: bank TAC notices, card alerts, deliveries, receipts, promotions,
│              government reminders, work, family and casual chat (incl. "untung")
└── heldout/   50 messages (25 scam, 25 legit), written without seeing the rules. Never tuned on.
```

- One message per file. Header lines start with `#` and record kind, type and language.
- **No real personal data.** Names are fictional; numbers, accounts and links are made up (ADR-007).
- Scam samples are paraphrased from common patterns in public scam-awareness material, never copied from private messages.
- The test prints a per-sample report (level, score, rules fired), so tuning changes can be compared run to run.
- Samples 033–052 in `scam/` and `legit/` were held-out set 1. They became tuning samples once the rules were changed after seeing them.

### How held-out sets are made and used

1. A writer who has **never seen the rule files or the other samples** writes the set: Malaysian SMS and WhatsApp style, in BM, English and mixed, with informal spellings.
2. It is measured **without changing any rule**.
3. If the rules are then improved using its misses, it moves into `scam/` and `legit/`, and a **new** held-out set is written to measure the change, again with the rules frozen first.

`SampleSetAccuracyTest` enforces G2 on the held-out set but only **reports** its scam detection. Enforcing G1 there would push us to tune to it, and then it would no longer be held out.

### Results (2026-11-24)

| Round | Rules | Tuning set: scams MEDIUM+ | Tuning set: legit HIGH | **Held-out: scams MEDIUM+** | **Held-out: legit HIGH** | Held-out: legit MEDIUM |
|---|---|---|---|---|---|---|
| M5 | 8 keyword categories | 32/32 (100%) | 0/32 | — | — | — |
| M6, held-out set 1 | unchanged from M5 | 32/32 (100%) | 0/32 | **5/20 (25%)** | **0/20** | 0/20 |
| M6, held-out set 2 | broader vocabulary, non-bank brand lookalikes | 48/52 (92%) | 0/52 | **10/25 (40%)** | **0/25** | 1/25 |
| Target | | ≥ 90% | ≤ 10% | ≥ 90% | ≤ 10% | |

**The honest reading:**
- **G2 holds on unseen messages.** No normal message in either held-out set was rated HIGH. One LHDN reminder was rated MEDIUM ("LHDN" plus "tarikh akhir").
- **G1 holds on the tuning set but not on unseen messages.** The rules catch explicit, common scam wording well, but subtle scams written in fresh words are mostly missed: 40% on set 2.
- The M6 changes helped. Branded fake links (J&T, LHDN, Touch 'n Go, gov.my) and "account will be deactivated"-style threats are now caught. But each new held-out set brings wording no list has seen. **Keyword lists alone seem to plateau well below 90%** on realistic, unseen messages.

### What the rules still miss (held-out set 2)

| Cause | Held-out examples |
|---|---|
| **Fresh wording for a known trick.** The same scam in words no list has | "yuran guaman" (a legal fee as the advance fee, k06); "forward kat sy" (asking for a code, k15); "claim the payment … masukkan details bank" (k08); a holiday "deposit to secure your dates" (k13) |
| **Scam types with no signal at all** | Friend account takeover (k17), an APK file posing as a wedding card (k18), QR code to install an app (k22), a fake court notice (k23), a job moved to Telegram (k24), an insurance refund app (k16) |
| **One signal alone stays LOW by design** (ADR-005: one MODERATE is 20, under 25) | Task job (k05), "new number" without a money request yet (k11), rental with no viewing (k14), "3–5% daily" crypto (k20) |

These are recorded as v1 limitations. Better coverage likely needs signals beyond phrase lists, for example:
- relationship claims combined with a request ("I'm your child/friend" + money or a code);
- app installs and APK attachments;
- scoring several weak signs as more than one moderate sign.

Each of those is a design change for a later release (§8), not more phrases.

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

- Go beyond phrase lists (§4): relationship-claim plus request patterns, APK and app-install detection, and a review of how several weak signs combine (an ADR amending ADR-005). Measure each change on a fresh held-out set.
- Browser end-to-end tests (e.g. Playwright) for the manual checklist
- Accessibility audit with a screen reader and an automated contrast check
- Load test on the live host to confirm NFR-01 including network time
- Mutation testing (e.g. PIT) on `scamcheck-core` to measure test strength beyond coverage
