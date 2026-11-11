package edu.helpdesk;

import java.util.*;

public class RequestService {
  private final RequestRepository repository;
  private final RequestIdGenerator ids;
  private final RequestValidator validator;
  private final Map<String, PriorityRule> rules;
  private final Map<String, NotificationSender> senders;

  public RequestService(
      RequestRepository repository,
      RequestIdGenerator ids,
      RequestValidator validator,
      Map<String, PriorityRule> rules,
      Map<String, NotificationSender> senders) {
    this.repository = Objects.requireNonNull(repository);
    this.ids = Objects.requireNonNull(ids);
    this.validator = Objects.requireNonNull(validator);
    this.rules = Map.copyOf(rules);
    this.senders = Map.copyOf(senders);
  }

  public HelpdeskRequest submit(String name, String type, String description, String channel) {
    validator.validate(name, type, description, channel, rules.keySet(), senders.keySet());
    String normalizedType = type.trim(), normalizedChannel = channel.trim();
    HelpdeskRequest request =
        new HelpdeskRequest(
            ids.next(),
            name.trim(),
            normalizedType,
            description.trim(),
            rules.get(normalizedType).priority(),
            normalizedChannel);
    repository.save(request);
    senders.get(normalizedChannel).send(request);
    return request;
  }

  public List<HelpdeskRequest> list() {
    return repository.list();
  }
}
