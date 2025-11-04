package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import org.json.simple.JSONObject;
import org.tinylog.Logger;

public class HistoryHandler implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    // parse out symbol of interest from URL
    String[] requestURLParts = exchange.getRequestURI().getRawPath().split("/");
    String symbol = requestURLParts[requestURLParts.length - 1];

    JSONObject json = new JSONObject();
    int statusCode;

    //        try {
    //            double price = analyticsComputor.currentPrice(symbol);
    //            json.put("price", price);
    //            statusCode = 200;
    //        } catch (UnknownBuoyException e) {
    //            json.put("error", e.getMessage());
    //            statusCode = 404;
    //        }

    String response = json.toString();

    Logger.info("Handled price request for {}, responding with {}.", symbol, response);

    exchange.getResponseHeaders().set("Content-Type", "application/json");
    //        exchange.sendResponseHeaders(statusCode, response.length());

    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(response.getBytes());
    }
  }
}
