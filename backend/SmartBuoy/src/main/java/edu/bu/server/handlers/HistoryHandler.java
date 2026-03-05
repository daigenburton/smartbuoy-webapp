package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
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

    // parse path for measurement type, buoyId, and hours param
    String measurementType = getMeasurementTypeFromPath(exchange);
    Integer buoyId = getBuoyIdFromPath(exchange);
    Integer hours = getHoursFromPath(exchange);

    JSONObject json = new JSONObject();
    int statusCode;

    try {
      // filter by hours if param is present
      List<BuoyResponse> history = getFilteredHistory(buoyId, hours);

      // build response
      JSONArray responseArray = buildHistoryArray(history, buoyId, measurementType);
      json.put("history", responseArray);
      statusCode = 200;

    } catch (UnknownBuoyException e) {
      json.put("error", e.getMessage());
      statusCode = 404;
    } catch (NumberFormatException e) {
      json.put("error", "Invalid buoy id");
      statusCode = 400;
    }

    sendJsonResponse(exchange, json, statusCode);
    Logger.info(
        "Handled history request for {}, responding with {}.", buoyId.toString(), json.toString());
  }

  private int getBuoyIdFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getPath().split("/");
    try {
      return Integer.parseInt(parts[3]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid buoy id");
    }
  }

  private String getMeasurementTypeFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getPath().split("/");
    return parts[2].toLowerCase();
  }

  private Integer getHoursFromPath(HttpExchange exchange) {
    String query = exchange.getRequestURI().getQuery();
    if (query == null) return null;

    for (String param : query.split("&")) {
      if (param.startsWith("hours=")) {
        try {
          return Integer.parseInt(param.split("=")[1]);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid hours parameter");
        }
      }
    }
    return null;
  }

  private List<BuoyResponse> getFilteredHistory(int buoyId, Integer hours)
      throws UnknownBuoyException {

    List<BuoyResponse> history = dataStore.getHistory(buoyId);

    if (hours == null) return history;

    Instant cutoff = Instant.now().minusSeconds(hours * 3600L);

    return history.stream()
        .filter(r -> r.getTimestamp().isAfter(cutoff))
        .collect(Collectors.toList());
  }

  private JSONObject buildEntry(BuoyResponse response, int buoyId, String measurementType) {

    JSONObject entry = new JSONObject();
    entry.put("buoyId", buoyId);
    entry.put("measurementType", measurementType);

    switch (measurementType) {
      case "temperature":
        entry.put("temperature", response.getTemperature());
        break;
      case "pressure":
        entry.put("pressure", response.getPressure());
        break;
      case "location":
        entry.put("latitude", response.getLatitude());
        entry.put("longitude", response.getLongitude());
        break;
      default:
        throw new IllegalArgumentException("Invalid measurement type");
    }

    entry.put("timestamp", response.getTimestamp().toEpochMilli());
    return entry;
  }

  private JSONArray buildHistoryArray(
      List<BuoyResponse> history, int buoyId, String measurementType) {

    JSONArray array = new JSONArray();

    for (BuoyResponse r : history) {
      array.add(buildEntry(r, buoyId, measurementType));
    }

    return array;
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
