package edu.helpdesk;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/** Composition root: changes to registrations belong here, not in the workflow. */
public final class ApplicationFactory {
  private ApplicationFactory() {}

  public static RequestService create() {
    AtomicLong sequence = new AtomicLong();
    Logger logger = Logger.getLogger("helpdesk.simulated");
    return new RequestService(
        new InMemoryRequestRepository(),
        () -> "REQ-%04d".formatted(sequence.incrementAndGet()),
        new BasicRequestValidator(),
        Map.of("IT", new ITPriorityRule(), "HR", new HRPriorityRule()),
        Map.of(
            "EMAIL",
            new SimulatedEmailSender(logger::info),
            "SMS",
            new SimulatedSmsSender(logger::info)));
  }
}
