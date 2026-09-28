# Coding challenge: remote-start-service (60 minutes)

You just joined the team that owns this service. **`README.md` is the spec.** Use your AI tools as
you normally would. We care about how you work: how you read the code, how you check what the AI
produces, your tests, and your reasoning. Talk us through your decisions.

**Commit after each task**, with a message that says why.

---

## Task 0: Orientation (5 min)

Build it and skim it. Then tell us in 2 minutes what happens between the driver tapping **Start** and
the app showing "Charging", and what looks risky to you.

## Task 1: The build is red (10 min)

`mvn test` fails. Make it green.

## Task 2: Incident, two sessions for one tap (20 min)

> **From two CPOs:** "Since last Tuesday you often send us two START_SESSION commands for the same
> driver a few seconds apart. Some drivers ended up with two sessions on two connectors."
>
> **From the team:** "Last Tuesday we scaled the service from 1 to 3 replicas for the winter peak. In
> the access logs, the duplicate POSTs carry the same Idempotency-Key."
>
> The unit tests for idempotency are green.

Find the root cause, fix it so the README's idempotency contract holds, and prove it with tests. Be
ready to explain what happens when the app's retry arrives **while the first request is still waiting
for the CPO**, and what the app gets back in that case.

## Task 3: Ticket CHG-1423 (20 min)

> **CHG-1423: Remote start timeout**
>
> As a driver, when I start charging from the app and nothing happens, I want to know within about a
> minute, so I'm not left standing at the charger waiting.
>
> **Acceptance criteria**
> * A remote start that has had no CommandResult 60 seconds after it was sent is set to `TIMED_OUT`.
> * The driver is notified.

## Task 4: Discussion, if time is left (5 min)

* `RemoteStartService` calls the CPO inside a `@Transactional` method. The read timeout is 30 s and each
  replica has 10 DB connections. What happens at peak when a big CPO is slow? What would you change?
* Look at `CommandResultHandler`. Can the CommandResult arrive before the remote start is committed?
  What happens then?
* Should we retry `START_SESSION` when the call to the CPO times out?
