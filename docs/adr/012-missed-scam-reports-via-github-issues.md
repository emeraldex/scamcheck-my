# ADR-012: Missed-scam reports through a public GitHub issue form

- **Status:** Accepted
- **Date:** 2026-11-24
- **Follows up:** [ADR-008](008-privacy-stateless-no-input-logging.md), which said the missed-scam report path "must be an explicit, user-initiated action with its own privacy review"
- **Resolves:** plan risk R9 (rules go stale), plan M6 "missed-scam report path"

## Context

The rules only improve if we hear about the messages they get wrong. ADR-008 rules out collecting inputs: the app stores and logs nothing. So a report has to be something the user chooses to send, knowing where it goes and who can read it.

## Decision

- The result view shows **"Did we get this wrong? Report a missed scam or a false alarm"**. It links, in a new tab, to a **GitHub issue form** (`.github/ISSUE_TEMPLATE/missed-scam.yml`).
- **The app sends nothing.** The link carries no text, level or other data from the page. The user types or pastes the report on GitHub themselves.
- The link is followed by a plain warning: *"This opens a public page on GitHub. Remove names, phone numbers and account numbers before you post."* (EN and BM).
- The form repeats the warning first, asks for the message **with personal details replaced** by `[name]`, `[phone]` and `[account]`, and has two required checkboxes: details removed, and "this is public and not a police report".
- The form points people who've lost money to **NSRC 997** and **Semak Mule**, as the app does.
- Maintainers triage reports as in [MAINTAINING.md](../MAINTAINING.md): turn a valid report into a fictional sample, then change the rule data.

## Consequences

- **No new data handling in the app.** No endpoint, storage or secret, and ADR-008 holds unchanged.
- **Reports are public.** Some users will still paste personal details despite the warnings. A maintainer must edit or delete such content quickly (GitHub lets the repository owner edit or delete issues), and the maintainer guide says so.
- **Needs a GitHub account.** That limits who can report, but also filters out spam. It's acceptable for v1.
- **Samples stay fictional.** Reports are never copied into the sample sets as is. They're rewritten with made-up names, numbers and links (ADR-007).

## Alternatives considered

- **A report endpoint that stores submissions:** contradicts ADR-008 and the plan's non-goal of storing user content. It would need moderation, retention rules and a privacy notice.
- **Pre-fill the issue with the analysed text via URL parameters:** puts personal data in a URL (browser history, GitHub logs) before the user can clean it up. Rejected.
- **An email link (`mailto:`):** private, but needs a monitored inbox, and reports can't be tracked publicly against rule changes.
- **A third-party form service:** sends user content to another processor, with its own privacy terms.
