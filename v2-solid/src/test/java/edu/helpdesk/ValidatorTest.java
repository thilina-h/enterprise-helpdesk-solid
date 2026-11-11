package edu.helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ValidatorTest {
  private final RequestValidator validator = new BasicRequestValidator();

  private void validate(String name, String type, String description, String channel) {
    validator.validate(
        name, type, description, channel, Set.of("IT", "HR"), Set.of("EMAIL", "SMS"));
  }

  @Test
  void boundariesAndTrimming() {
    assertDoesNotThrow(() -> validate(" a ", " IT ", " h ", " EMAIL "));
    assertDoesNotThrow(() -> validate("a".repeat(50), "HR", "h".repeat(200), "SMS"));
  }

  @Test
  void missingAndUnallowlistedValues() {
    String[][] invalid = {
      {null, "IT", "Help", "EMAIL"},
      {"Ada", null, "Help", "EMAIL"},
      {"Ada", "IT", null, "EMAIL"},
      {"Ada", "IT", "Help", null},
      {"Ada", "it", "Help", "EMAIL"},
      {"Ada", "IT", "Help", "email"}
    };
    for (String[] row : invalid)
      assertThrows(IllegalArgumentException.class, () -> validate(row[0], row[1], row[2], row[3]));
  }

  @Test
  void rendererEscapesAllHtmlMetacharacters() {
    assertEquals("&lt;script&gt;&amp;&quot;&#39;", HtmlRenderer.escape("<script>&\"'"));
  }
}
