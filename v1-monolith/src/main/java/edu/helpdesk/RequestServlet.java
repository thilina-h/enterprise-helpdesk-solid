package edu.helpdesk;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/** Intentionally monolithic educational design; functionality must remain correct. */
public class RequestServlet extends HttpServlet {
  private final AtomicLong sequence = new AtomicLong();
  private final Map<Long, Entry> entries = new ConcurrentSkipListMap<>();
  private static final Logger LOG = Logger.getLogger(RequestServlet.class.getName());

  record Entry(
      String id, String name, String type, String description, String priority, String channel) {}

  static String escape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  static String validate(String name, String type, String description, String channel) {
    if (name == null || name.trim().isEmpty() || name.trim().length() > 50)
      return "Student name must contain 1–50 characters.";
    if (!Set.of("IT", "HR").contains(type == null ? "" : type.trim())) return "Choose IT or HR.";
    if (description == null || description.trim().isEmpty() || description.trim().length() > 200)
      return "Description must contain 1–200 characters.";
    if (!Set.of("EMAIL", "SMS").contains(channel == null ? "" : channel.trim()))
      return "Choose EMAIL or SMS.";
    return null;
  }

  static String priority(String type) {
    // TEACHING: adding another request type grows this decision chain.
    if (type.equals("IT")) return "HIGH";
    else if (type.equals("HR")) return "MEDIUM";
    throw new IllegalArgumentException("Unsupported request type");
  }

  private void page(HttpServletResponse res, String body) throws IOException {
    res.setContentType("text/html;charset=UTF-8");
    res.getWriter()
        .write(
            "<!doctype html><html lang='en'><meta charset='UTF-8'><title>Enterprise"
                + " Helpdesk</title><style>body{font:18px system-ui;max-width:850px;margin:3rem"
                + " auto;background:#eef3f8;color:#142B49}main{background:#FFFFFF;padding:2rem}label{display:block;margin:1rem"
                + " 0}button{background:#C92D35;color:white;padding:.6rem}td,th{padding:.5rem;text-align:left}</style><main><h1>Enterprise"
                + " Helpdesk</h1><p>Local educational demo · Data is lost on restart ·"
                + " Notifications are SIMULATED</p><nav><a href='request'>New request</a> | <a"
                + " href='requests'>Submitted requests</a></nav>"
                + body
                + "</main></html>");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // TEACHING: routing, persistence access and rendering share one class.
    if (req.getServletPath().equals("/requests")) {
      StringBuilder body = new StringBuilder("<h2>Submitted requests</h2>");
      if (entries.isEmpty()) body.append("<p>No requests yet.</p>");
      else {
        body.append("<table><tr><th>ID</th><th>Name</th><th>Type</th><th>Priority</th></tr>");
        for (Entry e : entries.values())
          body.append(
              "<tr><td>"
                  + escape(e.id())
                  + "</td><td>"
                  + escape(e.name())
                  + "</td><td>"
                  + escape(e.type())
                  + "</td><td>"
                  + escape(e.priority())
                  + "</td></tr>");
        body.append("</table>");
      }
      page(res, body.toString());
    } else
      page(
          res,
          "<h2>New request</h2><form method='post' action='request'><label>Student name <input"
              + " name='name' required maxlength='50'></label><label>Request type <select"
              + " name='type'><option>IT</option><option>HR</option></select></label><label>Problem"
              + " description <textarea name='description' required"
              + " maxlength='200'></textarea></label><label>Notification method <select"
              + " name='channel'><option>EMAIL</option><option>SMS</option></select></label><button>Submit"
              + " request</button></form>");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    if (!req.getServletPath().equals("/request")) {
      res.setStatus(405);
      res.setHeader("Allow", "GET");
      page(res, "<h2>Method not allowed</h2>");
      return;
    }
    req.setCharacterEncoding("UTF-8");
    String name = req.getParameter("name"),
        type = req.getParameter("type"),
        description = req.getParameter("description"),
        channel = req.getParameter("channel");
    // TEACHING: HTTP, validation, business rules, saving and notifications are mixed.
    String error = validate(name, type, description, channel);
    if (error != null) {
      res.setStatus(400);
      page(res, "<h2>Invalid request</h2><p>" + escape(error) + "</p>");
      return;
    }
    long number = sequence.incrementAndGet();
    Entry e =
        new Entry(
            "REQ-%04d".formatted(number),
            name.trim(),
            type.trim(),
            description.trim(),
            priority(type.trim()),
            channel.trim());
    entries.put(number, e);
    // TEACHING: concrete logging notification is tightly coupled to this servlet.
    LOG.info("SIMULATED " + e.channel() + " notification for " + e.id() + "; no real message sent");
    page(
        res,
        "<h2>Request received</h2><p>ID: "
            + escape(e.id())
            + "</p><p>Student: "
            + escape(e.name())
            + "</p><p>Type: "
            + escape(e.type())
            + "</p><p>Priority: "
            + escape(e.priority())
            + "</p><p>Notification: "
            + escape(e.channel())
            + " — SIMULATED; no real message sent</p>");
  }
}
