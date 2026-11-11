# SOLID code map

V1 source names below refer to `v1-monolith/src/main/java/edu/helpdesk`; V2 names refer to `v2-solid/src/main/java/edu/helpdesk` unless explicitly under `src/test`. Each demo takes 3–5 minutes; exact commands are in [the classroom script](03-classroom-demo-script.md).

## S — Single Responsibility

A class should have one coherent responsibility and one main reason to change.

**Before:** `v1-monolith/src/main/java/edu/helpdesk/RequestServlet.java`: `doPost`, `doGet`, `page` mix transport, workflow, storage and presentation.

**After:** `v2-solid/src/main/java/edu/helpdesk/RequestServlet.java`: `doPost` delegates to `RequestService.submit`; `HtmlRenderer.confirmation`, `BasicRequestValidator.validate`, `InMemoryRequestRepository.save` and `SimulatedEmailSender.send` own separate responsibilities.

**3–5 minute demonstration:** Show a hypothetical wording change in V1 `doPost` LOG message, then V2 `SimulatedEmailSender.send`. Run the SRP command in the demo script; confirmation and workflow still pass.

**Ask:** Where should email simulation wording change? **Expected:** In V2 `SimulatedEmailSender.send`; V1 forces editing the servlet.

**Avoid:** SRP does not mean one method per class; both senders can share a contract while each class has a coherent purpose.

## O — Open/Closed

Keep stable processing logic closed to edits while allowing a defined kind of extension through new implementations.

**Before:** `RequestServlet.priority` has an IT/HR if/else chain; its `validate` and form also know allowed types.

**After:** `RequestService.submit` looks up a `PriorityRule` in an immutable registry; `ApplicationFactory.create` registers IT/HR. `RequestServiceTest.FinancePriorityRule` and `financeExtensionChangesRegistrationOnly` add LOW in test composition.

**3–5 minute demonstration:** Run the Finance test; inspect the new rule and registration while leaving `RequestService.submit` unchanged.

**Ask:** Does OCP forbid editing the factory? **Expected:** No. Composition is an expected change point; the stable rule-processing algorithm stays unchanged.

**Avoid:** OCP is bounded. A public Finance feature needs UI, validation text and contract documentation changes too.

## L — Liskov Substitution

An implementation must honour the behaviour promised by its abstraction so clients can substitute it safely.

**Before:** V1 has no sender abstraction; its concrete LOG call in `doPost` makes a useful substitution contract hard to express. This is not evidence of an existing subtype violation.

**After:** `NotificationSender.send` documents supported inputs and event behaviour; `SimulatedEmailSender.send` and `SimulatedSmsSender.send` satisfy `SenderContractTest.contract`. `SenderContractTest.BadSender` is test-only.

**3–5 minute demonstration:** Run shared contract tests, then explicitly enable the bad sender test; valid sends fail only in the opt-in example.

**Ask:** Why is BadSender invalid while null rejection is allowed? **Expected:** It refuses promised valid inputs; the contract explicitly rejects null before recording.

**Avoid:** Exceptions do not automatically violate LSP. Contract-permitted failure is valid; refusing every supported request is not.

## I — Interface Segregation

Clients should depend on focused contracts rather than methods they do not need.

**Before:** V1 has no interface hierarchy; do not invent an ISP violation. Its `doPost` mixes operations that motivate the separate oversized-interface snippet.

**After:** `RequestRepository.save/list` and `NotificationSender.send` expose different roles. `docs/examples/InterfaceSegregation.java` contrasts them with a bloated non-runtime interface.

**3–5 minute demonstration:** Read and compile the snippet; compare its contracts with V2. Run the repository replacement test and identify which unrelated methods the fake avoids.

**Ask:** Why should a sender not implement list or deleteAll? **Expected:** Those operations belong to storage/administration, not notification.

**Avoid:** ISP does not require every interface to have exactly one method; save/list form a useful repository role here.

## D — Dependency Inversion

High-level workflow should depend on contracts whose implementations are supplied from outside.

**Before:** `RequestServlet.doPost` directly selects priority, saves into its own concrete map and logs a concrete notification.

**After:** `RequestService` constructor accepts `RequestRepository`, `RequestIdGenerator`, `RequestValidator`, rule and sender maps. `ApplicationFactory.create` owns concrete construction. `RequestServiceTest` supplies fakes.

**3–5 minute demonstration:** Run the two dependency swap tests; inspect fake sender/repository and verify `RequestService.java` needs no edit.

**Ask:** Where is it acceptable to construct InMemoryRequestRepository? **Expected:** In the composition root, which deliberately knows concrete choices.

**Avoid:** DIP is about dependency direction, not adding interfaces everywhere or requiring Spring/CDI.
