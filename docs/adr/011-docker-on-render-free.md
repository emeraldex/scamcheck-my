# ADR-011: One Docker image, deployed to Render's free tier

- **Status:** Accepted
- **Date:** 2026-11-24
- **Resolves:** NFR-08 (portability), plan risk R8 (free host limits or changes), the M5 live URL

## Context

Version 1 needs a public URL that a recruiter can open, at no cost. Free hosting changes often (R8), so the app must not depend on one host's build system. The service is stateless (ADR-008): no database, no secrets, no disk. The host only needs to run one container and send it HTTP traffic.

## Decision

- **Package the app as one Docker image** (`Dockerfile` at the repo root):
  - A multi-stage build: the JDK image builds the jar, and the run image has only the JRE 21 (`eclipse-temurin:21-jre-noble`).
  - Spring Boot's layered extraction puts dependencies and our code in separate layers.
  - The app runs as a non-root user.
- **Deploy to Render's free web service**, described as code in `render.yaml`:
  - Docker runtime, Singapore region (closest to users in Malaysia).
  - Health check on `/actuator/health`.
  - Auto-deploys `main` only after its CI checks pass (`autoDeployTrigger: checksPass`).
- **Expose only `/actuator/health`**, with no details. Every other actuator endpoint is turned off and returns 404 (`ActuatorExposureTest`).
- **CI builds the image and smoke-tests the running container** on every pull request:
  - the health check;
  - a known scam is rated HIGH;
  - the security headers are present;
  - `/actuator/env` is not exposed;
  - the app doesn't run as root.

## Consequences

- **Cold starts.** Render's free instances spin down after about 15 minutes without traffic. The first request after that waits roughly a minute while the container starts. The README says so. NFR-01 (p95 < 500 ms) applies to a warm instance.
- **Portability.** Moving to another Docker host (Koyeb, Fly.io, Google Cloud Run, a VPS) needs only that host's config file. The image doesn't change.
- **Privacy.** Render's request logs record the method, path and status, never request bodies, so pasted text isn't stored by the host either (ADR-008). There are no secrets to manage.
- **Memory.** The free tier gives 512 MB. The JVM is capped at 75% of the container's memory, with the serial collector and C1-only JIT for a small footprint and quick start. CI runs the smoke test with `--memory 512m`.
- **Base image updates.** Dependabot opens weekly PRs for the Docker base images, as it does for Maven and Actions.

## Alternatives considered

- **Fly.io:** good Docker support, but no longer has a free allowance for new accounts.
- **Railway:** a trial credit only, then paid.
- **Koyeb free tier:** a workable fallback. It's kept as the plan B if Render's free tier changes, and needs no image change.
- **Build with Render's native Java runtime instead of Docker:** ties the build to one host, the opposite of R8's mitigation.
- **Expose more actuator endpoints (info, metrics):** not needed for v1, and each one is more surface to secure.
