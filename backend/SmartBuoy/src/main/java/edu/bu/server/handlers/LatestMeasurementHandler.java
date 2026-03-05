package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Optional;
import org.json.simple.JSONValue;
import org.tinylog.Logger;

/**
 * Handles requests like /temp/{id}, /pressure/{id}, /location/{id} and returns the latest
 * measurement.
 */
public class LatestMeasurementHandler implements HttpHandler {

  final DataStore dataStore;
  final String measurementType;

  public LatestMeasurementHandler(DataStore dataStore, String measurementType) {
    this.dataStore = dataStore;
    this.measurementType = measurementType;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    int statusCode;

    try {
      int buoyId = Integer.parseInt(getBuoyIdFromPath(exchange));
      statusCode = processRequest(buoyId, exchange);

    } catch (UnknownBuoyException e) {
      sendError(exchange, e.getMessage(), 404);
      statusCode = 404;
    } catch (NumberFormatException e) {
      sendError(exchange, "Invalid buoy id", 400);
      statusCode = 400;
    }

    Logger.info(
        "Handled latest {} request for buoy {}, responding with {}.",
        measurementType,
        getBuoyIdFromPath(exchange),
        statusCode);
  }

  private int processRequest(int buoyId, HttpExchange exchange)
      throws IOException, UnknownBuoyException {
    Optional<BuoyResponse> latest = dataStore.getLatest(buoyId);

    if (latest.isPresent()) {
      if ("location".equalsIgnoreCase(measurementType)) {
        return handleLocation(latest.get(), exchange);
      } else {
        return handleScalarMeasurement(latest.get(), exchange);
      }
    } else {
      sendError(exchange, "No data for buoy " + buoyId, 404);
      return 404;
    }
  }

  private String getBuoyIdFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getRawPath().split("/");
    return parts[parts.length - 1];
  }

  @SuppressWarnings("unchecked")
  private int handleScalarMeasurement(BuoyResponse response, HttpExchange exchange)
      throws IOException {
    LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
    ordered.put("buoyId", response.getBuoyId());
    ordered.put("measurementType", measurementType);

    if ("temperature".equalsIgnoreCase(measurementType)) {
      ordered.put("temperature", response.getTemperature());
    } else if ("pressure".equalsIgnoreCase(measurementType)) {
      ordered.put("pressure", response.getPressure());
    } else {
      sendError(exchange, "Unknown measurement type: " + measurementType, 400);
      return 400;
    }

    ordered.put("timestamp", response.getTimestamp().toEpochMilli());
    sendJsonResponse(exchange, ordered, 200);
    return 200;
  }

  @SuppressWarnings("unchecked")
  private int handleLocation(BuoyResponse response, HttpExchange exchange) throws IOException {
    LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
    ordered.put("buoyId", response.getBuoyId());
    ordered.put("measurementType", measurementType);
    ordered.put("latitude", response.getLatitude());
    ordered.put("longitude", response.getLongitude());
    ordered.put("timestamp", response.getTimestamp().toEpochMilli());
    sendJsonResponse(exchange, ordered, 200);
    return 200;
  }

  private void sendJsonResponse(
      HttpExchange exchange, LinkedHashMap<String, Object> map, int statusCode) throws IOException {
    String response = JSONValue.toJSONString(map);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, response.length());
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(response.getBytes());
    }
  }

  private void sendError(HttpExchange exchange, String message, int statusCode) throws IOException {
    LinkedHashMap<String, Object> error = new LinkedHashMap<>();
    error.put("error", message);
    sendJsonResponse(exchange, error, statusCode);
  }
}
