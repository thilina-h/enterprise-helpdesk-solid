package edu.helpdesk;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class HtmlRenderer {
  public static String escape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  public void page(HttpServletResponse res, String body) throws IOException {
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

  public void form(HttpServletResponse res) throws IOException {
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

  public void listing(HttpServletResponse res, List<HelpdeskRequest> entries) throws IOException {
    StringBuilder body = new StringBuilder("<h2>Submitted requests</h2>");
    if (entries.isEmpty()) body.append("<p>No requests yet.</p>");
    else {
      body.append("<table><tr><th>ID</th><th>Name</th><th>Type</th><th>Priority</th></tr>");
      for (HelpdeskRequest e : entries)
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
  }

  public void confirmation(HttpServletResponse res, HelpdeskRequest e) throws IOException {
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
