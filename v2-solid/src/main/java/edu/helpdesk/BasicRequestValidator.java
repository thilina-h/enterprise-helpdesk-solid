package edu.helpdesk;

import java.util.Set;

public class BasicRequestValidator implements RequestValidator {
  public void validate(
      String name,
      String type,
      String description,
      String channel,
      Set<String> types,
      Set<String> channels) {
    if (name == null || name.trim().isEmpty() || name.trim().length() > 50)
      throw new IllegalArgumentException("Student name must contain 1–50 characters.");
    if (type == null || !types.contains(type.trim()))
      throw new IllegalArgumentException("Choose IT or HR.");
    if (description == null || description.trim().isEmpty() || description.trim().length() > 200)
      throw new IllegalArgumentException("Description must contain 1–200 characters.");
    if (channel == null || !channels.contains(channel.trim()))
      throw new IllegalArgumentException("Choose EMAIL or SMS.");
  }
}
