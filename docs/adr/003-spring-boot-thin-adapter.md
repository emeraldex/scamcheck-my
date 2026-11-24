# ADR-003: Spring Boot 4 on Java 21 as a thin adapter

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

The Project Plan fixes Java 21 and multi-module Maven, and says the framework is "likely Spring Boot", to be confirmed here. The service needs: one JSON endpoint, request validation, standard error responses, static file serving, localised messages, a health check, and an easy container build for a free host.

The project's audience includes Malaysian employers hiring Java developers, where Spring Boot is the most common backend stack.

## Decision

Use **Spring Boot 4.1** (Spring Framework 7) on **Java 21**, confined to the `scamcheck-api` module. Its jobs are:

- the HTTP adapter (`AnalyzeController`, request and response DTOs);
- validation (Jakarta Bean Validation) and RFC 9457 error responses (`ProblemDetail`);
- wiring the core's `ScamAnalyzer` and its rules as beans in one `@Configuration` class;
- localisation (`MessageSource`) and serving the static page;
- operations: Actuator health check, `PORT` environment variable, layered jar for Docker.

The Spring Boot BOM is **imported** in the parent POM (`<dependencyManagement>`, `scope=import`) rather than used as the `<parent>`, so the project keeps its own parent POM.

## Consequences

- Familiar, employer-relevant stack with very little boilerplate.
- Validation, error format, i18n and health checks come mostly from configuration.
- Startup time (a few seconds) adds to free-host cold starts. Acceptable per Plan §6, noted in the README.
- Importing the BOM means the Spring Boot Maven plugin version must be set explicitly in `pluginManagement`.
- Business logic stays out of Spring ([ADR-004](004-framework-free-core-module.md)), so the framework could be swapped without touching the rules.

## Alternatives considered

- **Quarkus or Micronaut:** faster startup and smaller memory use, attractive for free hosts. Less common in local job adverts, and the startup gain matters little for a demo.
- **Plain JDK `HttpServer` or Javalin:** minimal and fast, but validation, i18n, error handling and health checks would all be hand-written, which is a poorer showcase of typical production skills.
- **Spring Boot 3.5:** more tutorials available, but 4.x is current and new projects should start on it.
