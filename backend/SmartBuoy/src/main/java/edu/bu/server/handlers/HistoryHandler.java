package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.io.OutputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tinylog.Logger;

/** Handles HTTP requests to the /history endpoint and returns buoy data */
public class HistoryHandler implements HttpHandler {

  final DataStore dataStore;

  public HistoryHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    String strID = getBuoyIdFromPath(exchange);
    JSONObject json = new JSONObject();
    int statusCode;

    try {
      int buoyId = Integer.parseInt(strID);
      JSONArray array = new JSONArray();

      for (BuoyResponse r : dataStore.getHistory(buoyId)) {
        JSONObject obj = new JSONObject();
        fillJsonResponse(obj, r);
        array.add(obj);
      }

      json.put("history", array);
      statusCode = 200;

    } catch (UnknownBuoyException e) {
      json.put("error", e.getMessage());
      statusCode = 404;
    }

    sendJsonResponse(exchange, json, statusCode);
    Logger.info("Handled history request for {}, responding with {}.", strID, json.toString());
  }

  private String getBuoyIdFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getRawPath().split("/");
    return parts[parts.length - 1];
  }

  private void fillJsonResponse(JSONObject json, BuoyResponse response) {
    json.put("buoyId", response.buoyId);
    json.put("measurementType", response.measurementType);
    json.put("measurementVal", response.measurementVal);
    json.put("timestamp", response.timestamp);
  }

  private void sendJsonResponse(HttpExchange exchange, JSONObject json, int statusCode)
      throws IOException {
    String response = json.toString();
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, response.length());
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(response.getBytes());
    }
  }
}
