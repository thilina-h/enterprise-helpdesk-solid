package edu.helpdesk;

import java.util.*;

public class InMemoryRequestRepository implements RequestRepository {
  private final NavigableMap<Long, HelpdeskRequest> requests = new TreeMap<>();

  public synchronized void save(HelpdeskRequest request) {
    long number = Long.parseLong(request.id().substring(4));
    if (requests.putIfAbsent(number, request) != null)
      throw new IllegalArgumentException("Duplicate ID");
  }

  public synchronized List<HelpdeskRequest> list() {
    return List.copyOf(requests.values());
  }
}
