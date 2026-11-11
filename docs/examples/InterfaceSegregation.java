// NON-RUNTIME teaching snippet. Compile independently; never included in the WAR.
final class InterfaceSegregation {
  interface BloatedHelpdeskManager {
    void save(String id);

    java.util.List<String> list();

    void send(String id);

    void deleteAll();

    void exportPayroll();
  }

  // A sender implementing the bloated interface must answer storage/payroll questions.
  // These focused roles let each client ask only for the behaviour it uses.
  interface RequestStorage {
    void save(String id);

    java.util.List<String> list();
  }

  interface RequestNotification {
    void send(String id);
  }

  interface RequestAdministration {
    void deleteAll();
  }

  interface PayrollExport {
    void exportPayroll();
  }

  private InterfaceSegregation() {}
}
