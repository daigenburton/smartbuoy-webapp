package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.notifications.Notification;
import edu.bu.analytics.notifications.NotificationService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.json.simple.JSONArray;

/** Handles HTTP requests to the /alerts endpoint */
public class AlertsHandler implements HttpHandler {

  private final NotificationService notificationService;

  public AlertsHandler(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  private void addCorsHeaders(HttpExchange exchange) {
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    addCorsHeaders(exchange);

    if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
      exchange.sendResponseHeaders(204, -1);
      return;
    }

    String method = exchange.getRequestMethod();

    String query = exchange.getRequestURI().getQuery();
    String userId = null;

    if (query != null && query.startsWith("userId=")) {
      userId = query.split("=")[1];
    }

    // mark all as read
    if (method.equalsIgnoreCase("POST")) {
      if (userId != null) {
        notificationService.markAllRead(userId);
      }
      exchange.sendResponseHeaders(200, -1);
      return;
    }

    // only allow GET beyond this point
    if (!method.equalsIgnoreCase("GET")) {
      exchange.sendResponseHeaders(405, -1);
      return;
    }

    final String finalUserId = userId;

    List<Notification> all = notificationService.getNotifications();

    List<Notification> filtered =
        all.stream().filter(n -> finalUserId == null || n.getUserId().equals(finalUserId)).toList();

    JSONArray array = new JSONArray();
    for (Notification n : filtered) {
      array.add(n.toJSON());
    }

    String response = array.toJSONString();

    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, response.length());

    try (OutputStream output_stream = exchange.getResponseBody()) {
      output_stream.write(response.getBytes());
    }
  }
}
