package edu.helpdesk;

import java.util.Set;

/**
 * Reject invalid input before workflow side effects; throw IllegalArgumentException with safe
 * guidance.
 */
public interface RequestValidator {
  void validate(
      String name,
      String type,
      String description,
      String channel,
      Set<String> types,
      Set<String> channels);
}
