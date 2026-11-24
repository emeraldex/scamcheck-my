# ScamCheck MY — Maintainer Guide

How to keep the rules current (plan risk R9): handle reports, add phrases, add rules and release. Every change still goes through a PR with green CI ([CONTRIBUTING](../CONTRIBUTING.md)).

## 1. Handling a report

Reports come from the **"Report a missed scam or a false alarm"** link on each result. It opens the [issue form](../.github/ISSUE_TEMPLATE/missed-scam.yml) (ADR-012), and new reports are labelled `rule-report`.

1. **Check for personal data first.** Issues are public. If a report has a real name, phone number, account number, IC number or a link with someone's details, edit it out straight away (**⋯ → Edit**). Delete the issue if it can't be cleaned. Do this before anything else.
2. **Reproduce it.** Paste the cleaned message into the [live site](https://scamcheck-my.onrender.com) or `POST /api/v1/analyze` and note the level, the score and which rules fired.
3. **Decide:**
   - **Missed scam:** which signal should have fired? A missing phrase (§2), a phrase in a new category (§3), or a weight that's too low (§4)?
   - **False alarm:** which finding was wrong? Usually a phrase that's too general. Remove it, or make it longer and more specific.
   - **Working as designed:** for example, one weak sign alone is meant to stay LOW (ADR-005). Reply to explain, and close the issue.
4. **Add a sample first.** Write a fictional version of the message as a new file in `scamcheck-core/src/test/resources/samples/scam/` or `legit/` (TEST_PLAN §4). Make up the names, numbers and links. **Never paste the reported text as is** (ADR-007).
5. **Change the data** (§2–§4), run the accuracy test (§5), and open a PR that says `Closes #<issue>`.

## 2. Adding or changing a keyword phrase

Keyword lists live in `scamcheck-core/src/main/resources/rules/keywords/`, one file per category. Each line is:

```
WEIGHT   | language | phrase
STRONG   | ms       | akaun selamat
MODERATE | en       | work from home
```

- **Weight:** `STRONG` (35 points), `MODERATE` (20), `WEAK` (10), `INFO` (5). Only the strongest finding in each category counts (ADR-005).
- **Language:** `ms` or `en`. Matching ignores case, extra spaces and look-alike Unicode forms (full-width letters, curly apostrophes), and only matches whole words.
- **Pick phrases that scammers use and normal messages don't.** "Bank in" alone is too common, while "tolong bank in" is a stronger sign. Add a comment above anything that looks surprising.
- **Each phrase once per file.** The loader rejects duplicates, and the build fails.
- Update the `Last reviewed:` date at the top of the file.

Other data files in `rules/`: `bank-domains.txt` (official bank domains for the lookalike check), `url-shorteners.txt`, `unusual-tlds.txt`, and `tlds.txt` (used to spot links written without `https://`).

## 3. Adding a new keyword category

For example, a new scam type that doesn't fit any existing category:

1. Add a value to `Category` in `scamcheck-core/.../model/Category.java`.
2. Create `rules/keywords/<name>.txt` with the usual header comment.
3. Register it in `KeywordRules.defaults()` with the ID `keyword.<name>`.
4. Add EN and BM text to `messages_en.properties` and `messages_ms.properties`: `keyword.<name>.title` and `keyword.<name>.explanation`. `MessageBundleCompletenessTest` fails if either is missing.
5. Add the category to the enum in `docs/api/openapi.yaml`, and to the rules table in `docs/ARCHITECTURE.md`.
6. Add the new ID to `KeywordRulesTest.loadsAllKeywordRulesWithUniqueIds`, with positive and negative tests (NFR-07).

## 4. Adding a rule that isn't a keyword list

Implement `Rule` (`id()`, `category()`, `evaluate(ExtractedInput)`) in the right `rule/` package. Read links, phones and accounts from `ExtractedInput` rather than parsing the text again (ADR-002). Then add it to `DefaultRules.all()`, add messages as in §3 step 4, and write unit tests. `ScamAnalyzerTest.Wiring` shows a rule plugging in without touching the engine (NFR-06).

Changing a weight or threshold in `ScoringPolicy` is a scoring-model change. Record it in a new ADR that amends ADR-005.

## 5. Checking accuracy

```bash
./mvnw -pl scamcheck-core test -Dtest=SampleSetAccuracyTest
```

The test prints every sample with its level, score and the rules that fired, then checks G1 (≥ 90% of scams rated MEDIUM or HIGH) and G2 (≤ 10% of legitimate messages rated HIGH). In the PR, compare the report with the one from `main`. A fix for one report must not flip another sample.

## 6. Dependencies

Dependabot opens weekly PRs for Maven, GitHub Actions and the Docker base images. Merge each one when both CI checks pass. Major Java versions are skipped on purpose: moving Java is a deliberate change to the POM, CI and the Dockerfile together.

## 7. Releasing

1. Move the items under `## [Unreleased]` in [CHANGELOG.md](../CHANGELOG.md) into a new version section, following semantic versioning:
   - a new rule or category is a **minor** version;
   - data-only tuning and fixes are a **patch**.
2. Set the same version in all three `pom.xml` files.
3. Merge the release PR, then tag `main`: `git tag -a vX.Y.Z -m "vX.Y.Z"` and `git push origin vX.Y.Z`.
4. Create a GitHub release from the tag, with the changelog section as its notes.

Render deploys every green commit on `main`, so tagging marks a release. It doesn't trigger a deploy.
