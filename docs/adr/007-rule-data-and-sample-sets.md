# ADR-007: Rule data and sample sets as versioned text files

- **Status:** Accepted
- **Date:** 2026-11-24
- **Resolves:** SRS open questions Q2 (genuine bank domains), Q3 (account formats), Q5 (sample sets)

## Context

Keyword phrases, genuine bank domains, URL shorteners and "unusual" domain endings will change more often than rule logic (R9). Changing them shouldn't mean editing Java. They must also be reviewable in a PR, including by a native BM speaker who isn't a Java developer (R7).

Accuracy goals G1/G2 need scam and legitimate sample messages, which must not contain real people's personal data.

The core module can't use Spring to load resources or a YAML library without good reason ([ADR-004](004-framework-free-core-module.md)).

## Decision

### Rule data files

Plain UTF-8 text files in `scamcheck-core/src/main/resources/rules/`, loaded once at startup with the JDK (`ClassLoader.getResourceAsStream`):

```
rules/
├── keywords/
│   ├── investment.txt
│   ├── urgency.txt
│   ├── account-threat.txt
│   ├── impersonation.txt
│   └── credential-request.txt
├── bank-domains.txt
├── url-shorteners.txt
└── unusual-tlds.txt
```

Keyword file format, one phrase per line:

```
# weight | language | phrase
STRONG   | ms       | dijamin untung
STRONG   | en       | guaranteed profit
WEAK     | ms       | untung
MODERATE | ms       | segera
```

- `#` starts a comment. Matching is case-insensitive, whitespace-normalised and word-bounded (SRS US-03 AC5–AC6).
- A loader test fails the build on malformed lines, unknown weights or duplicate phrases.

### Q2 — Genuine bank domains

`bank-domains.txt` lists each Malaysian bank's brand tokens and its official registrable domains, for example:

```
# brand tokens            | official domains
maybank, maybank2u        | maybank.com, maybank2u.com.my, maybank.com.my
```

- Sources: each bank's own website and Bank Negara Malaysia's public list of licensed banks, checked **by hand**. Nothing is scraped.
- `link.lookalike-bank` fires when a URL's host contains a brand token (or is one edit away from an official domain) but its registrable domain isn't on the official list.
- Registrable domain = last two labels, or last three for Malaysian second-level suffixes (`com.my`, `org.my`, `net.my`, `gov.my`, `edu.my`). This is a small built-in list, not the full Public Suffix List. That's good enough for Malaysian banks and documented as a known limitation.
- The list is reviewed at each release, with the date of the last review recorded at the top of the file.

### Q3 — Bank account formats

Version 1 uses **one generic pattern**: 10–16 digits, optionally grouped with spaces or hyphens, not already claimed as a phone number. Per-bank formats go on the "Later" list.

Reason: the finding's only job is to remind the user to check Semak Mule (US-06). It never claims an account is a mule account, so per-bank precision adds little value for its cost.

### Q5 — Sample sets

```
scamcheck-core/src/test/resources/samples/
├── scam/         one message per file, e.g. scam-001-investment-ms.txt
└── legit/        e.g. legit-001-bank-otp-notice-en.txt
```

- **Scam samples** are paraphrased by hand from public scam-awareness material (police, bank and news advisories) and common patterns. They are never copied wholesale or collected from private chats.
- **Legitimate samples** are written to resemble real bank notices, delivery updates and casual chat, including tricky words like "untung" in normal use.
- **No real personal data:** names are fictional; phone numbers, account numbers and URLs use clearly fake values (e.g. `example.com`, made-up account digits).
- Each file starts with a header comment noting its source type and language.
- A parameterised JUnit test runs the full engine over both sets and asserts NFR-02 (≥ 90% of scam samples Medium or High) and NFR-03 (≤ 10% of legitimate samples High). Target: at least 30 samples of each kind by Phase 5.

## Consequences

- Keyword changes are one-line PRs that a non-developer can review.
- No YAML/JSON dependency in the core; the parser is about 30 lines plus tests.
- Rule data ships inside the jar, so changes need a redeploy. Hot reload is on the "Later" list.
- The bank list goes stale without regular reviews; the "last reviewed" date makes that visible.
- The generic account pattern will sometimes match other long numbers (e.g. order IDs). Its `INFO` weight keeps that harmless.

## Alternatives considered

- **Hard-coded Java lists:** type-safe, but every wording change is a code change, and BM reviewers can't easily take part.
- **YAML or JSON files:** richer structure, but needs a parser dependency in the core for very flat data.
- **Database or admin UI:** contradicts "stateless, nothing stored" (ADR-008) and is out of scope.
- **Full Public Suffix List library:** more correct domain parsing, but an extra dependency to update for a problem that, in v1, only involves Malaysian bank domains.
