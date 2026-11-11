package edu.helpdesk;

public record HelpdeskRequest(
    String id, String name, String type, String description, String priority, String channel) {}
