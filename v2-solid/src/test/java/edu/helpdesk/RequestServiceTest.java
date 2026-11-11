package edu.helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class RequestServiceTest {
  @Test
  void invalidInputsHaveNoSideEffectsAndSenderCanBeSwapped() {
    InMemoryRequestRepository repository = new InMemoryRequestRepository();
    List<HelpdeskRequest> sent = new ArrayList<>();
    int[] sequence = {0};
    RequestService service =
        new RequestService(
            repository,
            () -> "REQ-%04d".formatted(++sequence[0]),
            new BasicRequestValidator(),
            Map.of("IT", new ITPriorityRule(), "HR", new HRPriorityRule()),
            Map.of("EMAIL", sent::add, "SMS", sent::add));
    assertThrows(IllegalArgumentException.class, () -> service.submit(" ", "IT", "Help", "EMAIL"));
    assertTrue(repository.list().isEmpty());
    assertTrue(sent.isEmpty());
    assertEquals(0, sequence[0]);
    HelpdeskRequest result = service.submit(" Ada ", " HR ", " Help ", " SMS ");
    assertEquals("MEDIUM", result.priority());
    assertEquals("Ada", result.name());
    assertEquals(List.of(result), sent);
    assertEquals(sent, repository.list());
    assertThrows(UnsupportedOperationException.class, () -> repository.list().clear());
  }

  @Test
  void repositoryCanBeReplacedWithoutChangingService() {
    List<HelpdeskRequest> stored = new ArrayList<>();
    RequestRepository fake =
        new RequestRepository() {
          public void save(HelpdeskRequest r) {
            stored.add(r);
          }

          public List<HelpdeskRequest> list() {
            return List.copyOf(stored);
          }
        };
    RequestService service =
        new RequestService(
            fake,
            () -> "REQ-0001",
            new BasicRequestValidator(),
            Map.of("IT", new ITPriorityRule()),
            Map.of("EMAIL", r -> {}));
    service.submit("Ada", "IT", "Help", "EMAIL");
    assertEquals(1, stored.size());
  }

  @Test
  void financeExtensionChangesRegistrationOnly() {
    RequestService service =
        new RequestService(
            new InMemoryRequestRepository(),
            () -> "REQ-0001",
            new BasicRequestValidator(),
            Map.of(
                "IT",
                new ITPriorityRule(),
                "HR",
                new HRPriorityRule(),
                "FINANCE",
                new FinancePriorityRule()),
            Map.of("EMAIL", r -> {}));
    assertEquals("LOW", service.submit("Ada", "FINANCE", "Help", "EMAIL").priority());
  }

  // Test-only extension: baseline factory and UI still offer only IT/HR.
  static class FinancePriorityRule implements PriorityRule {
    public String priority() {
      return "LOW";
    }
  }
}
