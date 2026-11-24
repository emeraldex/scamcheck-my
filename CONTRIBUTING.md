# Contributing to ScamCheck MY

This is a solo portfolio project, but it follows a team workflow on purpose. See [Project Plan §9](docs/PROJECT_PLAN(rev'd).md) for the reasoning.

## Branching: GitHub Flow

- `main` is always buildable and deployable. No direct pushes; every change lands through a pull request that passes CI.
- One short-lived branch per user story or task, named `<type>/<short-description>`:

  | Type | Example |
  |---|---|
  | `feature/` | `feature/US-03-keyword-rules` |
  | `fix/` | `fix/phone-regex-plus60` |
  | `docs/` | `docs/srs` |
  | `test/` | `test/link-rule-cases` |
  | `chore/` | `chore/ci-setup` |
  | `build/` | `build/maven-skeleton` |

- Use the story ID from the [SRS](docs/SRS.md) in feature branch names.
- PRs are **squash merged**, so `main` gets one clean commit per story. The PR title becomes that commit message, so write it as a Conventional Commit.

## Commit messages: Conventional Commits

```
<type>(<optional scope>): <summary in the imperative, lower case, no full stop>
```

Types: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`, `build`.
Suggested scopes: `rules`, `link`, `phone`, `account`, `engine`, `api`, `ui`, `i18n`.

Examples:

```
feat(rules): add Malay urgency keywords
fix(link): handle uppercase shortener domains
docs: add ADR-002 rule engine
test(phone): cover +60 edge cases
```

## Pull request self-review

Before merging, check:

- [ ] The branch is named after its story or task
- [ ] The PR title is a Conventional Commit
- [ ] Acceptance criteria from the SRS are covered by tests (for `feature/` PRs)
- [ ] No user input is logged or stored (SRS US-10)
- [ ] CI is green
- [ ] Docs (README, ADRs, CHANGELOG) are updated where needed

## Releases

Tags follow semantic versioning (`v0.1.0` … `v1.0.0`) and each release is recorded in `CHANGELOG.md`.
