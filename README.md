# ScamCheck MY

[![CI](https://github.com/emeraldex/scamcheck-my/actions/workflows/ci.yml/badge.svg)](https://github.com/emeraldex/scamcheck-my/actions/workflows/ci.yml)

Paste a suspicious message, link, phone number or bank account number and get the warning signs found, a risk level (Low / Medium / High), and links to official places to check or report: PDRM Semak Mule and the NSRC hotline 997.

**Live demo: https://scamcheck-my.onrender.com**. It runs on a free instance, so the first visit after a quiet spell can take about a minute while it wakes up.

> Screenshots will be added in milestone M6.
> Not affiliated with PDRM, NSRC or any bank. This tool gives advice only.

## How accurate is it?

ScamCheck MY uses explainable rules (keyword lists, link checks, phone and account checks), not machine learning. On messages written by someone who never saw the rules:

- **No normal message was rated HIGH risk** (0 of 45).
- **It caught 40% of scams** (10 of 25), mostly the explicit ones: fake bank, courier, e-wallet and government links, frozen-account threats, and requests for codes or fees. Subtle scams written in fresh words often come back LOW.

So a **LOW result never means "safe"**, and the page says so. The [test plan](docs/TEST_PLAN.md#4-sample-sets) has the full method and numbers.

## Documentation

| Document | What it covers |
|---|---|
| [Project Plan](docs/PROJECT_PLAN(rev'd).md) | Problem, goals, scope, risks, timeline |
| [SRS](docs/SRS.md) | User stories, acceptance criteria, non-functional requirements |
| [Architecture](docs/ARCHITECTURE.md) | Modules, request flow, rule interface, scoring |
| [ADRs](docs/adr/) | Why each design decision was made |
| [API spec](docs/api/openapi.yaml) | OpenAPI 3.1 for `POST /api/v1/analyze` |
| [Wireframe](docs/WIREFRAME.md) | Page layout |
| [Test plan](docs/TEST_PLAN.md) | Test levels, sample sets and accuracy results, coverage, manual checklist |
| [Deployment](docs/DEPLOYMENT.md) | Docker image, Render setup, cold starts, health check |
| [Contributing](CONTRIBUTING.md) | Branching, commit messages, PR checklist |

## Modules

| Module | What it holds | Depends on |
|---|---|---|
| `scamcheck-core` | Domain model and rules engine. Plain Java 21, no framework, enforced by the build ([ADR-004](docs/adr/004-framework-free-core-module.md)). | nothing |
| `scamcheck-api` | Spring Boot REST API and the web page ([ADR-003](docs/adr/003-spring-boot-thin-adapter.md)). | `scamcheck-core` |

## Build and run

Requires JDK 21. The Maven wrapper downloads the right Maven version.

```bash
./mvnw verify
./mvnw -pl scamcheck-api -am spring-boot:run
```

On Windows PowerShell, use `.\mvnw.cmd` instead of `./mvnw`.

### With Docker

```bash
docker build -t scamcheck .
docker run --rm -p 8080:8080 scamcheck
```

Then open http://localhost:8080. The health check is at `/actuator/health`. See [Deployment](docs/DEPLOYMENT.md) for the live setup on Render.

### Tests and coverage

`verify` runs all tests and fails if line coverage drops below 90% in either module. The coverage report is in `<module>/target/site/jacoco/index.html`. CI runs the same command on every pull request, then builds the Docker image and smoke-tests the running container.

## Try the API

With the app running:

```bash
curl -s -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{"text": "Pelaburan dijamin untung! Daftar segera dan berikan kod TAC.", "language": "en"}'
```

The response holds the risk level (`LOW` / `MEDIUM` / `HIGH`), the score, each warning sign found with a plain-language explanation, and links to Semak Mule and NSRC 997. `language` is `en` or `ms` (default `ms`). See the [API spec](docs/api/openapi.yaml) for the full contract.

## Licence

[MIT](LICENSE)
