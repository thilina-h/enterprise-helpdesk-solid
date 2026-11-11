# Student investigation (25 minutes, ungraded, no programming)

Use an editor and the running baseline applications. Do not modify source code. Write brief answers in your own words; compare with a partner at the end.

## 0–5 minutes: observe behaviour

Open `/helpdesk-v1/request` and `/helpdesk-v2/request`. Submit the same IT/EMAIL request in each, then HR/SMS. Compare priority, channel and listing pages. Submit again: are IDs reused? What survives a restart?

Expected: HIGH for IT, MEDIUM for HR; each valid submission creates a new ID. Nothing is durable. Distinct histories can produce different IDs without a parity bug.

## 5–10 minutes: identify a responsibility

Compare V1 `RequestServlet.doPost` with V2 `RequestServlet.doPost` and `RequestService.submit`. Circle the lines that validate, save and notify. Which principle explains the split? Suppose notification formatting changes: which class should be opened in each version?

Expected: SRP; V1 servlet versus V2 sender. The servlet remains responsible for HTTP concerns.

## 10–15 minutes: trace an extension

Read V1 `priority`, V2 `RequestService.submit`, `ApplicationFactory.create`, and `RequestServiceTest.financeExtensionChangesRegistrationOnly`. Without writing code, list what adding Finance would require in a test and what a public UI rollout would additionally require. Which stable method stays unchanged?

Expected: OCP; new rule and registration. A public rollout also needs form choices, validation guidance and matching baseline/feature documentation. `RequestService.submit` stays unchanged.

## 15–20 minutes: compare contracts

Read `NotificationSender` and `SenderContractTest.BadSender`. Does matching the method signature guarantee substitution? Is rejecting null allowed? Compare `RequestRepository` and `NotificationSender` with `docs/examples/InterfaceSegregation.java`.

Expected: LSP depends on promised behaviour; throwing for every valid request violates it. Null rejection is documented. ISP explains focused roles; repository clients need save/list but notification clients do not.

## 20–25 minutes: explain a hypothetical change

Inspect `ApplicationFactory.create` and the two dependency swap tests in `RequestServiceTest`. A teacher wants to collect notification events in a list instead of logging. Where would that replacement be supplied? Would RequestService need editing? Why is an interface unnecessary for every HTML helper?

Expected: DIP; inject a sender/recorder at composition or in a unit test. Workflow stays unchanged. Abstractions are justified by useful roles and variation, not by a rule that every class needs one.

Exit question: name one thing SOLID improves here and one thing it does not supply. Possible answer: clearer change boundaries; it does not supply authentication, durable storage or MVC/JSP automatically.
