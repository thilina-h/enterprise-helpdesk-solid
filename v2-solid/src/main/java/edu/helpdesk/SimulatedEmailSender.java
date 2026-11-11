package edu.helpdesk;

import java.util.Objects;
import java.util.function.Consumer;

public class SimulatedEmailSender implements NotificationSender {
  private final Consumer<String> recorder;

  public SimulatedEmailSender(Consumer<String> recorder) {
    this.recorder = Objects.requireNonNull(recorder);
  }

  public void send(HelpdeskRequest request) {
    Objects.requireNonNull(request);
    recorder.accept("SIMULATED EMAIL notification for " + request.id() + "; no real message sent");
  }
}
