package edu.helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

class HttpContractTest {
  static class Response {
    int status = 200;
    String contentType;
    StringWriter body = new StringWriter();

    HttpServletResponse proxy() {
      return (HttpServletResponse)
          Proxy.newProxyInstance(
              getClass().getClassLoader(),
              new Class[] {HttpServletResponse.class},
              (p, m, a) -> {
                switch (m.getName()) {
                  case "setStatus":
                    status = (int) a[0];
                    return null;
                  case "setContentType":
                    contentType = (String) a[0];
                    return null;
                  case "getWriter":
                    return new PrintWriter(body);
                  default:
                    return null;
                }
              });
    }
  }

  static HttpServletRequest input(String path, Map<String, String> parameters) {
    return (HttpServletRequest)
        Proxy.newProxyInstance(
            HttpContractTest.class.getClassLoader(),
            new Class[] {HttpServletRequest.class},
            (p, m, a) ->
                switch (m.getName()) {
                  case "getServletPath" -> path;
                  case "getParameter" -> parameters.get(a[0]);
                  default -> null;
                });
  }

  static Map<String, String> valid(String type, String channel) {
    return Map.of(
        "name",
        " <script>Ada</script> ",
        "type",
        type,
        "description",
        "<script>help</script>",
        "channel",
        channel);
  }

  static Response get(RequestServlet servlet, String path) throws Exception {
    Response r = new Response();
    servlet.doGet(input(path, Map.of()), r.proxy());
    return r;
  }

  static Response post(RequestServlet servlet, String path, Map<String, String> params)
      throws Exception {
    Response r = new Response();
    servlet.doPost(input(path, params), r.proxy());
    return r;
  }

  @Test
  void baselineHttpContract() throws Exception {
    RequestServlet servlet = new RequestServlet();
    servlet.init();
    assertTrue(get(servlet, "/requests").body.toString().contains("No requests yet"));
    Response form = get(servlet, "/request");
    assertEquals("text/html;charset=UTF-8", form.contentType);
    assertTrue(form.body.toString().contains("<form"));
    for (String[] row : new String[][] {{"IT", "EMAIL", "HIGH"}, {"HR", "SMS", "MEDIUM"}}) {
      Response r = post(servlet, "/request", valid(row[0], row[1]));
      assertEquals(200, r.status);
      for (String expected :
          new String[] {
            "&lt;script&gt;Ada&lt;/script&gt;",
            row[0],
            row[1],
            row[2],
            "SIMULATED",
            "no real message sent"
          }) assertTrue(r.body.toString().contains(expected), expected);
      assertFalse(r.body.toString().contains("<script>"));
    }
    String before = get(servlet, "/requests").body.toString();
    for (String[] change :
        new String[][] {
          {"name", " "},
          {"name", "x".repeat(51)},
          {"type", "FINANCE"},
          {"description", ""},
          {"description", "x".repeat(201)},
          {"channel", "FAX"}
        }) {
      Map<String, String> params = new HashMap<>(valid("IT", "EMAIL"));
      params.put(change[0], change[1]);
      assertEquals(400, post(servlet, "/request", params).status);
    }
    assertEquals(before, get(servlet, "/requests").body.toString());
    assertEquals(405, post(servlet, "/requests", valid("IT", "EMAIL")).status);
  }

  @Test
  void notificationsContainOnlyMetadataAndInvalidInputDoesNotNotifyOrConsumeId() throws Exception {
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
    List<String> messages = new ArrayList<>();
    java.util.logging.Handler capture =
        new java.util.logging.Handler() {
          public void publish(java.util.logging.LogRecord event) {
            messages.add(event.getMessage());
          }

          public void flush() {}

          public void close() {}
        };
    logger.addHandler(capture);
    try {
      RequestServlet servlet = new RequestServlet();
      servlet.init();
      Map<String, String> invalid = new HashMap<>(valid("IT", "EMAIL"));
      invalid.put("channel", "FAX");
      assertEquals(400, post(servlet, "/request", invalid).status);
      assertTrue(messages.isEmpty());
      Response valid = post(servlet, "/request", valid("IT", "EMAIL"));
      assertTrue(valid.body.toString().contains("REQ-0001"));
      assertEquals(1, messages.size());
      String event = messages.getFirst();
      assertTrue(event.contains("SIMULATED EMAIL"));
      assertTrue(event.contains("REQ-0001"));
      assertTrue(event.contains("no real message sent"));
      assertFalse(event.contains("Ada"));
      assertFalse(event.contains("<script>"));
    } finally {
      logger.removeHandler(capture);
    }
  }

  @Test
  void concurrentSubmissionsHaveUniqueIds() throws Exception {
    RequestServlet servlet = new RequestServlet();
    servlet.init();
    try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
      List<Callable<String>> tasks = new ArrayList<>();
      for (int i = 0; i < 24; i++)
        tasks.add(
            () ->
                post(servlet, "/request", valid("IT", "SMS"))
                    .body
                    .toString()
                    .split("ID: ")[1]
                    .split("<")[0]);
      Set<String> ids = new HashSet<>();
      for (Future<String> result : pool.invokeAll(tasks)) ids.add(result.get());
      assertEquals(24, ids.size());
    }
  }
}
