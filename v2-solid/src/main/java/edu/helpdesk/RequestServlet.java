package edu.helpdesk;

import jakarta.servlet.http.*;
import java.io.IOException;

public class RequestServlet extends HttpServlet {
  private RequestService service;
  private final HtmlRenderer renderer = new HtmlRenderer();

  @Override
  public void init() {
    service = ApplicationFactory.create();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    if (req.getServletPath().equals("/requests")) renderer.listing(res, service.list());
    else renderer.form(res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    if (!req.getServletPath().equals("/request")) {
      res.setStatus(405);
      res.setHeader("Allow", "GET");
      renderer.page(res, "<h2>Method not allowed</h2>");
      return;
    }
    req.setCharacterEncoding("UTF-8");
    HelpdeskRequest request;
    try {
      request =
          service.submit(
              req.getParameter("name"),
              req.getParameter("type"),
              req.getParameter("description"),
              req.getParameter("channel"));
    } catch (IllegalArgumentException error) {
      res.setStatus(400);
      renderer.page(
          res, "<h2>Invalid request</h2><p>" + HtmlRenderer.escape(error.getMessage()) + "</p>");
      return;
    }
    renderer.confirmation(res, request);
  }
}
