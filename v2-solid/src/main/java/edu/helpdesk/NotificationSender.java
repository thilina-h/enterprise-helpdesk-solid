package edu.helpdesk;

/**
 * Accept a non-null, validated HelpdeskRequest with IT/HR type, HIGH/MEDIUM priority, EMAIL/SMS
 * channel, generated ID, and nonblank name/description within limits. Either implementation accepts
 * either channel; registry selects the requested transport. Record exactly one SIMULATED event
 * containing transport and ID, no personal data, return normally and never contact a destination.
 * Null is rejected before recording. Infrastructure failures may throw IllegalStateException;
 * success never means real delivery.
 */
@FunctionalInterface
public interface NotificationSender {
  void send(HelpdeskRequest request);
}
