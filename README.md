# cloud-itonami-isco-5312

Open Occupation Blueprint for **ISCO-08 5312**: Teachers’ Aides.

This repository designs a forkable OSS business for an independent teacher's aide: a classroom-support robot performs material setup and activity-station assistance under a governor-gated actor, so the practice keeps its own assistance and safety records instead of renting a closed classroom-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a classroom-support robot performs material setup and supervised activity-station assistance under an actor that proposes
actions and an independent **Teachers Aide Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
direct one-on-one supervision of a minor without a lead teacher present, or a behavioral-incident response) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
classroom plan + supervision ratio + safety protocol
        |
        v
Aide Advisor -> Teachers Aide Governor -> assist/monitor, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `5312`). Required capabilities:

- :robotics
- :identity
- :forms
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
