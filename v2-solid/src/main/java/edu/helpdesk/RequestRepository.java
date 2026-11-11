package edu.helpdesk;

import java.util.List;

/**
 * Save each unique ID once; list returns an immutable snapshot ordered by creation ID. Thread-safe.
 */
public interface RequestRepository {
  void save(HelpdeskRequest request);

  List<HelpdeskRequest> list();
}
