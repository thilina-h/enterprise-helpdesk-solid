# Architecture before and after

## Version 1: safe behaviour, mixed reasons to change

```mermaid
flowchart LR
 Browser -->|GET /request, POST /request, GET /requests| RequestServlet
 RequestServlet --> validate[validate helper]
 RequestServlet --> priority[priority if/else]
 RequestServlet --> ids[AtomicLong]
 RequestServlet --> storage[ConcurrentSkipListMap]
 RequestServlet --> logger[Logger: SIMULATED]
 RequestServlet --> html[page and inline HTML]
```

These helper boxes are methods/fields inside the same class, not independently injected collaborators. `Entry` is a nested immutable record. Any notification, validation or rendering edit brings the developer into the servlet.

## Version 2: composition and request flow

```mermaid
flowchart TD
 Factory[ApplicationFactory.create] -->|constructs once at servlet init| Service[RequestService]
 Factory --> Repo[InMemoryRequestRepository]
 Factory --> Email[SimulatedEmailSender]
 Factory --> SMS[SimulatedSmsSender]
 Factory --> IT[ITPriorityRule]
 Factory --> HR[HRPriorityRule]
 Browser --> Servlet[RequestServlet]
 Servlet --> Service
 Servlet --> Renderer[HtmlRenderer]
 Service --> Validator[RequestValidator]
 Validator -. implemented by .-> Basic[BasicRequestValidator]
 Service --> Repository[RequestRepository]
 Repository -. implemented by .-> Repo
 Service --> Senders[Map of NotificationSender]
 Senders -. implemented by .-> Email
 Senders -. implemented by .-> SMS
 Service --> Rules[Map of PriorityRule]
 Rules -. implemented by .-> IT
 Rules -. implemented by .-> HR
 Service --> IDs[RequestIdGenerator]
 Factory -->|atomic sequence lambda| IDs
 Service --> Model[HelpdeskRequest immutable record]
 Renderer -->|escaped HTML| Browser
```

The arrows from service to contracts describe compile-time dependencies. Factory arrows describe concrete construction. No runtime service locator is used. The servlet stores a shared service initialized safely by the container before handling requests; request input is never stored in servlet fields.

`submit` validates first, computes priority and ID, saves, then simulates notification. It returns an immutable model to the servlet for rendering. `list` returns an immutable repository snapshot. The factory registers rules and senders using immutable maps; adding a registration does not alter rule lookup. Test-only Finance and BadSender are absent from this runtime diagram and WAR.
