# remote-start-service

Lets drivers start charging from the app: the driver taps **Start** at a charger, and we send an OCPI
`START_SESSION` command to the CPO that operates it.

```
 driver app ──POST /remote-starts──▶ remote-start-service ──HTTP START_SESSION──▶ ocpi-adapter ──▶ CPO ──▶ charger
     │                                  │         ▲                                                          │
     └──── GET /remote-starts/{id} ─────┘         └──── Kafka: cpo.command-results ◀── ocpi-adapter ◀── CPO ◀┘
          (polls every 2 s)                  PostgreSQL
```

## Tech

Java 21 · Spring Boot 3.3 · Spring Kafka · Spring Data JPA · PostgreSQL 16 · **Liquibase** · JUnit 5 / Mockito / Testcontainers

## Running locally

```bash
docker compose up -d                                        # Postgres on 5433, Kafka on 9092
mvn spring-boot:run -Dspring-boot.run.profiles=local        # "local" uses a stub CPO that accepts everything
curl -i -X POST localhost:8080/api/v1/remote-starts -H 'Content-Type: application/json' \
     -H "Idempotency-Key: $(uuidgen)" -d '{"contractId":"DE-DCS-C12345678-X","evseUid":"DE*ABC*E1001*1"}'
./scripts/send-result.sh <id-from-response> ACCEPTED        # plays the CPO's asynchronous CommandResult
mvn test                                                    # the integration test needs Docker
```

## Production

* Runs as **3 replicas** behind a load balancer (no sticky sessions). DB pool: 10 connections per replica.
* ~20 remote starts/s at peak (evening), spread over ~900 CPOs of very different quality.
* The **app waits at most 10 s** for the POST response. On a timeout or network error it **retries the POST
  with the same `Idempotency-Key`** (up to 3 times), then polls `GET` every 2 s.

---

## REST API

### `POST /api/v1/remote-starts`

```json
{ "contractId": "DE-DCS-C12345678-X", "evseUid": "DE*ABC*E1001*1" }
```

* Both fields are required, otherwise `400`.
* The app sends an `Idempotency-Key` header (a UUID) with every POST. **Idempotency contract:**
  * The first request creates the remote start: `201` plus a `Location` header.
  * A retry with the **same key and the same body** within 24 h returns the original remote start (`200`) in
    its current status, and **must never send a second command to the CPO**.
  * The **same key with a different body** returns `422`.
* If the CPO cannot be reached or answers with an error (timeout, 5xx), the remote start is `FAILED` with reason
  `CPO_UNREACHABLE`, and the response is still `201`.

### `GET /api/v1/remote-starts/{id}`

Returns the remote start, or `404`.

## Remote start lifecycle

| From      | Event                                            | To        | `failureReason`            |
|-----------|--------------------------------------------------|-----------|----------------------------|
| –         | POST                                             | `PENDING` |                            |
| `PENDING` | CPO `CommandResponse` `ACCEPTED`                 | `SENT`    |                            |
| `PENDING` | CPO `CommandResponse` anything else              | `FAILED`  | `CPO_<response>`           |
| `PENDING` | CPO unreachable / error                          | `FAILED`  | `CPO_UNREACHABLE`          |
| `SENT`    | `CommandResult` `ACCEPTED`                       | `STARTED` |                            |
| `SENT`    | `CommandResult` anything else                    | `FAILED`  | the result, e.g. `EVSE_OCCUPIED` |

`STARTED` and `FAILED` are final. (`TIMED_OUT` exists in the code but is not implemented yet; see ticket CHG-1423.)

## CommandResult events (`cpo.command-results`)

* Produced by the OCPI adapter. Key = `command_id` (= our remote start id). OCPI naming (snake_case):

  ```json
  {"command_id":"6f1c…","result":"EVSE_OCCUPIED","message":"Connector in use"}
  ```
* Delivery is **at-least-once**.
* In OCPI the CPO answers `START_SESSION` synchronously with a `CommandResponse` that includes a `timeout`
  (seconds the CPO waits for the charger). The `CommandResult` comes later, usually within a few seconds, but it
  may come after that `timeout` for slow chargers.

## Driver notifications

Push notifications to drivers go through **notification-service**, which consumes `driver.notifications`. That
topic's message schema is owned by the notifications team (Avro, in the schema registry) and is **not part of
this repo**.
