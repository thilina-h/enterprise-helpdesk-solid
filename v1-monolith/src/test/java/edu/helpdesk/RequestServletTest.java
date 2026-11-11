package edu.helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequestServletTest {
  @Test
  void priorities() {
    assertEquals("HIGH", RequestServlet.priority("IT"));
    assertEquals("MEDIUM", RequestServlet.priority("HR"));
  }

  @Test
  void validation() {
    assertNull(RequestServlet.validate(" Ada ", " IT ", " Help ", " EMAIL "));
    assertNull(RequestServlet.validate("a".repeat(50), "HR", "x".repeat(200), "SMS"));
    String[][] invalid = {
      {null, "IT", "Help", "EMAIL"},
      {" ", "IT", "Help", "EMAIL"},
      {"a".repeat(51), "IT", "Help", "EMAIL"},
      {"Ada", "FINANCE", "Help", "EMAIL"},
      {"Ada", "IT", " ", "EMAIL"},
      {"Ada", "IT", "x".repeat(201), "EMAIL"},
      {"Ada", "IT", "Help", "FAX"}
    };
    for (String[] row : invalid)
      assertNotNull(RequestServlet.validate(row[0], row[1], row[2], row[3]));
  }

  @Test
  void escaping() {
    assertEquals("&lt;script&gt;&amp;&quot;&#39;", RequestServlet.escape("<script>&\"'"));
  }
}
