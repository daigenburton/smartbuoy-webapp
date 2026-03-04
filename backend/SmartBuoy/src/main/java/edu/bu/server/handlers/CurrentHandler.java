package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.io.OutputStream;
import org.json.simple.JSONObject;
import org.tinylog.Logger;

/** Handles HTTP requests to the /current endpoint */
public class CurrentHandler implements HttpHandler {

  private final DataStore dataStore;

  public CurrentHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    try {
      String measurementType = getMeasurementTypeFromPath(exchange);
      int buoyId = getBuoyIdFromPath(exchange);

      BuoyResponse latest = getLatestReading(buoyId);

      JSONObject response = buildResponse(latest, buoyId, measurementType);

      sendJsonResponse(exchange, response, 200);

    } catch (IllegalArgumentException e) {
      sendError(exchange, e.getMessage(), 400);
    } catch (UnknownBuoyException e) {
      sendError(exchange, e.getMessage(), 404);
    }

    Logger.info("Handled current request.");
  }

  private String getMeasurementTypeFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getPath().split("/");
    return parts[2].toLowerCase();
  }

  private int getBuoyIdFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getPath().split("/");
    try {
      return Integer.parseInt(parts[3]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid buoy id");
    }
  }

  private BuoyResponse getLatestReading(int buoyId) throws UnknownBuoyException {

    return dataStore
        .getLatest(buoyId)
        .orElseThrow(() -> new IllegalArgumentException("No data available for buoy"));
  }

  private JSONObject buildResponse(BuoyResponse response, int buoyId, String measurementType) {

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

  private void sendError(HttpExchange exchange, String message, int statusCode) throws IOException {

    JSONObject error = new JSONObject();
    error.put("error", message);
    sendJsonResponse(exchange, error, statusCode);
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
