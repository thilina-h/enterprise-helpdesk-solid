package edu.helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class SenderContractTest {
  static void contract(Function<Consumer<String>, NotificationSender> factory, String transport) {
    List<String> events = new ArrayList<>();
    NotificationSender sender = factory.apply(events::add);
    for (String channel : List.of("EMAIL", "SMS")) {
      HelpdeskRequest r =
          new HelpdeskRequest(
              "REQ-0001", "Private Name", "IT", "Private description", "HIGH", channel);
      int before = events.size();
      assertDoesNotThrow(() -> sender.send(r));
      assertEquals(before + 1, events.size());
      String event = events.getLast();
      assertTrue(event.contains("SIMULATED " + transport));
      assertTrue(event.contains(r.id()));
      assertTrue(event.contains("no real message sent"));
      assertFalse(event.contains(r.name()));
      assertFalse(event.contains(r.description()));
    }
    assertThrows(NullPointerException.class, () -> sender.send(null));
    assertEquals(2, events.size());
  }

  @TestFactory
  Stream<DynamicTest> substitutableSenders() {
    return Stream.of(
        DynamicTest.dynamicTest(
            "email contract", () -> contract(SimulatedEmailSender::new, "EMAIL")),
        DynamicTest.dynamicTest("SMS contract", () -> contract(SimulatedSmsSender::new, "SMS")));
  }

  /** Deliberately invalid teaching implementation: never wired into the application. */
  static class BadSender implements NotificationSender {
    public void send(HelpdeskRequest r) {
      throw new UnsupportedOperationException("Cannot send valid requests");
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "helpdesk.badLsp", matches = "true")
  void intentionalLspFailure() {
    contract(recorder -> new BadSender(), "EMAIL");
  }
}
