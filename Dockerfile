# syntax=docker/dockerfile:1
# ScamCheck MY: one container image for any Docker host (NFR-08, ADR-011).
# Tests run in CI, not here, so the image builds quickly on the host.

# --- Build: compile and package the Spring Boot jar ---
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /src

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY scamcheck-core/ scamcheck-core/
COPY scamcheck-api/ scamcheck-api/

# The cache mount keeps downloaded dependencies between builds on the same machine.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -DskipTests package

# Split the jar into layers: dependencies change rarely, our code changes often.
RUN java -Djarmode=tools -jar scamcheck-api/target/scamcheck.jar \
        extract --layers --destination /extracted

# --- Run: JRE only, no build tools, not root ---
FROM eclipse-temurin:21-jre-noble
WORKDIR /app

RUN groupadd --system scamcheck && useradd --system --gid scamcheck --no-create-home scamcheck

COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./

USER scamcheck

# Free hosts give about 512 MB. Let the heap use most of it and keep start-up quick.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "scamcheck.jar"]
