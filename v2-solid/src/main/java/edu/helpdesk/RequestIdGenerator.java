package edu.helpdesk;

@FunctionalInterface
public interface RequestIdGenerator {
  String next();
}
