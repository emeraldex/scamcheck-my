# ScamCheck MY — Project Plan (Phase 1)

| | |
|---|---|
| **Status** | Draft v0.1 — for review |
| **Author** | Azlan |
| **Last updated** | 2026-11-20 |
| **Planned duration** | 6 weeks, part-time (23 Nov 2026 – 3 Jan 2027) |

> Items marked **[DECIDE]** need your call before Phase 1 is signed off.

---

## 1. Problem statement

Malaysians are losing more money to online scams every year. Police data given in a Home Ministry parliamentary reply (Nov 2026) records **45,683 online scam cases with losses of RM1.18 billion in January–August 2026 alone**, and RM6.95 billion from 2023 to August 2026. Fake ("non-existent") investment schemes cause the biggest losses, followed by phone (telecommunication) scams.

Most victims are not caught by sophisticated hacking. They get a message that pushes them to act fast: an investment with guaranteed returns, a "bank" saying their account is frozen, or a link to a lookalike login page. The warning signs are well known, but people often don't spot them in the moment, and many don't know that official tools exist. PDRM's **Semak Mule** portal checks bank accounts and phone numbers, and the **NSRC 997** hotline runs 24 hours a day for scam victims.

**ScamCheck MY** gives a quick second opinion. The user pastes a suspicious message, link, phone number or bank account number. The app shows the warning signs it found, gives a risk rating (Low / Medium / High) and links to the official places to check or report.

**Sources**
- Home Ministry figures, reported by The Star and Malay Mail, 17 Nov 2026
- NSRC 997 information: malaysia.gov.my, Safety & Community → Cybersecurity
- Semak Mule: https://semakmule.rmp.gov.my

## 2. Target users

| User | Need | What this means for the design |
|---|---|---|
| **Everyday smartphone users** (primary) | "Is this WhatsApp/SMS message a scam?" | Mobile-first. Paste and get an answer in seconds. Plain language. |
| **Older adults and less tech-savvy users** | Clear, calm advice in their language | Malay and English. Large text. No jargon. A clear next step. |
| **Family members helping parents** | Something easy to explain and share | Results that can be explained on the screen. Clear links to Semak Mule and 997. |
| **Recruiters and technical reviewers** (portfolio audience) | Evidence of clean design and a professional process | README, SRS, ADRs, tests, CI, live demo. |

## 3. Goals

| # | Goal | How we'll measure it |
|---|---|---|
| G1 | Spot the common warning signs in scam messages | Rates ≥ 90% of the sample scam set as Medium or High |
| G2 | Avoid crying wolf on normal messages | ≤ 10% of the sample legitimate set rated High |
| G3 | Explain every result | Every warning shown names the rule that fired and gives a plain-language reason |
| G4 | Point users to official help | Every result links to Semak Mule and NSRC 997 |
| G5 | Support Malay and English | Rules and UI text work in both languages |
| G6 | Respond fast | Analysis endpoint p95 < 500 ms (excluding hosting cold start) |
| G7 | Show professional engineering | All documents complete, CI green, coverage target met (set in Phase 5), live URL |

## 4. Non-goals (version 1)

These are deliberately **out of scope**. Saying no early protects the timeline (YAGNI).

- **No scraping or automated querying of government sites** (Semak Mule, PDRM, etc.). We link to them only.
- **No claim to be official** and no PDRM/NSRC/bank logos. The app gives advice, not a verdict.
- **No storing of user-submitted content** (privacy by default).
- No user accounts or login.
- No machine learning or LLM-based classification. Rules only, so every result can be explained.
- No live domain reputation or WHOIS lookups through third-party APIs.
- No mobile app, browser extension, WhatsApp/Telegram bot or image (screenshot) input.
- No languages beyond English and Malay.

## 5. Scope for version 1

**Inputs:** one text box. The system works out what was pasted: a free-text message, a URL, a phone number or a bank account number. A message can contain more than one of these.

**Rule groups:**
1. **Scam keywords and phrases (EN + BM).** Investment and guaranteed returns ("pelaburan", "untung", "guaranteed profit"). Urgency ("segera", "act now"). Account threats ("akaun dibekukan", "account suspended"). Authority impersonation. Requests for a TAC/OTP.
2. **Suspicious links.** Lookalike Malaysian bank domains (e.g. `maybank2u-secure.xyz`). URL shorteners. Odd top-level domains. Raw IP addresses. Plain `http`.
3. **Phone numbers.** Malaysian format check (+60 / 01x). Flags formats that are invalid or unusual, and international numbers posing as local ones.
4. **Bank account numbers.** Basic format recognition only. We **can't** say whether an account is a mule account, so the app always sends the user to Semak Mule.

**Output:** a risk level (Low / Medium / High), the list of warning signs with an explanation for each, and links to Semak Mule, NSRC 997 and a "what to do now" tip. Shown in EN or BM.

**Delivery:** REST API plus a simple web UI, deployed to a free host with a public demo URL.

**[DECIDE]** Web UI approach. Server-rendered (Thymeleaf) keeps everything in Java. A small static HTML/JS page calling the API shows off the API more clearly. Record the choice as an ADR in Phase 3.

## 6. Assumptions and constraints

- Java 21, multi-module Maven. The framework (likely Spring Boot) gets confirmed by ADR in Phase 3.
- About 8–12 hours a week for 6 weeks (roughly 50–70 hours in total). **[DECIDE]** Confirm your real weekly hours.
- Free hosting only. Cold starts and resource limits are acceptable for a demo.
- Keyword and pattern lists are hand-curated from public scam awareness material, not from private data.
- The app is a self-help awareness tool, not a security product. Results always carry a disclaimer.

## 7. Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R1 | **False negatives.** A real scam rated Low, and the user trusts it. | Med | High | Never say "safe". Low means "no common warning signs found". Always show official links. Clear disclaimer. |
| R2 | **False positives.** Normal messages (e.g. real bank SMS, "untung" in casual chat) rated High. | High | Med | Weighted scoring, not one-hit-High. A sample legitimate test set (G2). Tune weights in Phase 5. |
| R3 | **Scope creep** (ML, bots, screenshots, live lookups) | High | High | Non-goals list. "Later" backlog. Phase gates. |
| R4 | **Time.** Part-time, plus year-end holidays in week 5–6. | High | Med | Week 6 is a buffer. Cut UI polish before cutting tests or docs. |
| R5 | **Privacy.** Users paste personal data (names, account numbers). | Med | High | No storage of input. No input in logs. HTTPS only. State this in the SRS and the UI. |
| R6 | **Misleading as official / legal.** Seen as impersonating PDRM or banks. | Low | High | No logos. Neutral branding. "Not affiliated" notice. Link, don't scrape (non-goal). |
| R7 | **Malay language quality.** Awkward phrasing or missed colloquial variants (e.g. "x", "utk"). | Med | Med | Collect real-style BM samples. Ask a native speaker to review the BM UI and keywords. |
| R8 | **Free host limits or changes** | Med | Low | Containerise (Docker) so the app can move hosts. Note cold start in the README. |
| R9 | **Rules go stale** as scam wording changes | High | Med | Pluggable rule design (Phase 3). Data-driven keyword lists. Documented "add a rule" process (Phase 7). |

## 8. Timeline and milestones

| Week | Dates | Phase(s) | Milestone (exit criteria) |
|---|---|---|---|
| 1 | 23–29 Nov | 1 Planning, 2 Requirements | **M1:** Plan approved. Repo live. SRS v1 with user stories and acceptance criteria. |
| 2 | 30 Nov – 6 Dec | 3 Design | **M2:** Architecture diagram, module layout, API spec, rule interface, wireframe, ADRs 001–00x. |
| 3 | 7–13 Dec | 4 Implementation (iteration 1) | **M3:** Walking skeleton. `POST /analyze` end to end with keyword rules (EN + BM) and scoring, TDD. |
| 4 | 14–20 Dec | 4 Implementation (iteration 2) | **M4:** Link, phone and account rules. Web UI with EN/BM toggle. Feature-complete v1. |
| 5 | 21–27 Dec | 5 Testing, 6 Deployment | **M5:** Test plan, sample data set, coverage target met, security checks. CI/CD green. **Live URL.** |
| 6 | 28 Dec – 3 Jan | 7 Maintenance + buffer | **M6:** Logging, missed-scam report path, CHANGELOG, v1.0.0 tag, README with screenshots. **Portfolio-ready.** |

*Week 6 doubles as buffer. If you're behind, finish M5 first and slim down M6.*

## 9. Repository and branching strategy

**Repo:** `scamcheck-my` on GitHub (public), MIT licence.

**Branching: GitHub Flow** (trunk-based with short-lived branches)
- `main` is always buildable and deployable. It is protected: no direct pushes, the PR must pass CI.
- One branch per user story or task, named by type:
  `feature/US-03-keyword-rules`, `fix/phone-regex-plus60`, `docs/srs`, `chore/ci-setup`, `test/link-rule-cases`.
- Open a PR to `main` even when working solo. Self-review against a checklist (added in Phase 4). **Squash merge** so `main` gets one clean commit per story.
- Releases are tagged with semantic versions (`v0.1.0`, …, `v1.0.0`) and recorded in CHANGELOG.

**Why not GitFlow?** GitFlow's `develop`/`release`/`hotfix` branches are built for teams shipping scheduled releases. For one developer deploying continuously, they add ceremony with no benefit (YAGNI).

**Commit messages: Conventional Commits**
`feat(rules): add Malay urgency keywords` · `fix(link): handle uppercase shortener domains` · `docs: add ADR-002 rule engine` · `test(phone): cover +60 edge cases`

This makes history readable and lets the CHANGELOG be generated later.

## 10. "Later" list (version 2+ ideas)

- Screenshot/image input (OCR)
- Browser extension or WhatsApp/Telegram bot
- Domain age and reputation lookups (via an official API, if one exists)
- Community "report a missed scam" moderation dashboard
- More languages (Chinese, Tamil)
- Rule configuration without redeploy (admin UI or hot-reloaded rule files)
- Shareable result card for family chats
- Statistics page of anonymised rule-hit counts

---

### Phase 1 sign-off checklist
- [x] Problem statement reviewed, sources cited
- [x] Target users agreed
- [x] Goals are measurable, non-goals agreed
- [x] v1 scope agreed; [DECIDE] items resolved
- [x] Risks reviewed, each with a mitigation
- [x] Timeline fits your real weekly hours
- [x] GitHub repo created; `main` protected; this plan committed under `docs/`
- [x] Branching and commit conventions written in `CONTRIBUTING.md` (or README)
