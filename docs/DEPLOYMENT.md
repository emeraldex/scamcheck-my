# ScamCheck MY — Deployment

| | |
|---|---|
| **Host** | Render, free web service, Singapore region |
| **Decision** | [ADR-011](adr/011-docker-on-render-free.md) |
| **Live URL** | *Added after the first deploy* |

The app ships as one Docker image. It is stateless and needs no database, secrets or environment variables, so any host that runs a container can serve it.

## How a change reaches the live site

1. A pull request runs CI: **Build and test** (Maven `verify`) and **Docker image** (build the image, start it in 512 MB, run smoke tests).
2. The PR is squash merged into `main`.
3. CI runs again on `main`. When its checks pass, Render builds the `Dockerfile` and deploys it (`autoDeployTrigger: checksPass` in [render.yaml](../render.yaml)).
4. Render switches traffic to the new container once `/actuator/health` returns `UP`. If it never does, the old version keeps serving.

## First-time setup (one-off, done by the repository owner)

1. Create a free account at [render.com](https://render.com) and connect GitHub, giving access to this repository only.
2. In the Render dashboard choose **New → Blueprint**, then pick `scamcheck-my`. Render reads `render.yaml` and shows one free web service called `scamcheck-my`.
3. Apply it. The first build takes a few minutes.
4. Open the `https://scamcheck-my-….onrender.com` URL Render shows. Run the manual checklist in [TEST_PLAN §6](TEST_PLAN.md#6-manual-test-checklist).
5. Put the URL in the table above and in the README.

## Free-tier limits

| Limit | Effect | What we do |
|---|---|---|
| Spins down after ~15 min idle | The first visit after that waits about a minute | The README warns about it. NFR-01 applies to a warm instance. |
| 512 MB memory, shared CPU | A JVM needs tuning to fit | Heap capped at 75% of memory, serial GC, C1 JIT (`JAVA_TOOL_OPTIONS` in the Dockerfile). CI tests with `--memory 512m`. |
| Monthly free instance hours | Enough for one always-available service | Only one service is deployed. |
| Free plans can change (plan risk R8) | The host may stop being free | The image is host-neutral. Plan B is Koyeb's free tier, which needs only its own config. |

## Run the image locally

```bash
docker build -t scamcheck .
docker run --rm -p 8080:8080 --memory 512m scamcheck
curl -s http://localhost:8080/actuator/health    # {"status":"UP"}
```

The container listens on `$PORT` (default 8080), which Render sets for you.

## Health check

`GET /actuator/health` returns `{"status":"UP"}` and nothing more. Every other actuator endpoint (`env`, `beans`, `heapdump` and so on) is turned off and returns 404. `ActuatorExposureTest` and the CI smoke test check this.

## Rolling back

In the Render dashboard, open the service, then **Events**, pick the last good deploy and choose **Rollback**. Then revert the bad commit on `main` with a PR, so the next deploy doesn't bring it back.

## Privacy on the host

Render's request logs show the method, path, status and timing, never request bodies. The app itself never logs pasted text ([ADR-008](adr/008-privacy-stateless-no-input-logging.md)). So the pasted message exists only in memory while the request is handled.
