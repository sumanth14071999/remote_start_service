# CLAUDE.md

Context for AI coding assistants working in this repo.

## Project
Spring Boot 3.3 / Java 21 service that starts charging sessions on behalf of drivers: it sends OCPI
START_SESSION commands to CPOs and consumes their asynchronous CommandResults from Kafka. `README.md` is
the spec (REST and idempotency contract, lifecycle, event contract, production setup). Read it before
changing behavior.

## Commands
- Build and unit tests: `mvn test`
- One test class: `mvn test -Dtest=RemoteStartServiceTest`
- Run locally with a stub CPO: `docker compose up -d`, then `mvn spring-boot:run -Dspring-boot.run.profiles=local`

## Database migrations (Liquibase)
- Master changelog: `src/main/resources/db/changelog/db.changelog-master.yaml`.
- One **formatted SQL** file per change in `db/changelog/changes/`, named `NNN-description.sql`, and
  included from the master changelog in order.
- Every changeset has an `author:id` and a `--rollback`.
- **Never edit a changeset that has been applied.** Add a new one.

## Conventions
- Time: `Instant` (UTC), always from the injected `Clock` so tests can control it.
- REST JSON is camelCase (public contract with the app). OCPI payloads are snake_case.
- The service runs as several replicas: no state that must be shared between requests may live in memory.
- Every behavior change comes with a test. Prefer plain unit tests; use Testcontainers for DB/Kafka
  behavior.
- Constructor injection only.
