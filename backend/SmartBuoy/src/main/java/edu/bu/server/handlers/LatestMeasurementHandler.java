package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import org.json.simple.JSONObject;
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

    JSONObject json = new JSONObject();
    int statusCode;

    try {
      int buoyId = Integer.parseInt(getBuoyIdFromPath(exchange));
      statusCode = processRequest(buoyId, json);

    } catch (UnknownBuoyException e) {
      json.put("error", e.getMessage());
      statusCode = 404;
    } catch (NumberFormatException e) {
      json.put("error", "Invalid buoy id");
      statusCode = 400;
    }

    sendJsonResponse(exchange, json, statusCode);
    Logger.info(
        "Handled latest {} request for buoy {}, responding with {}.",
        measurementType,
        getBuoyIdFromPath(exchange),
        json.toString());
  }

  private int processRequest(int buoyId, JSONObject json) throws UnknownBuoyException {
    Optional<BuoyResponse> latest = dataStore.getLatest(buoyId);

    if (latest.isPresent()) {
      if ("location".equalsIgnoreCase(measurementType)) {
        return handleLocation(latest.get(), json);
      } else {
        return handleScalarMeasurement(latest.get(), json);
      }
    } else {
      json.put("error", "No data for buoy " + buoyId);
      return 404;
    }
  }

  private String getBuoyIdFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getRawPath().split("/");
    return parts[parts.length - 1];
  }

  @SuppressWarnings("unchecked")
  private int handleScalarMeasurement(BuoyResponse response, JSONObject json) {
    json.put("buoyId", response.getBuoyId());
    json.put("measurementType", measurementType);

    if ("temperature".equalsIgnoreCase(measurementType)) {
      json.put("measurementVal", response.getTemperature());
    } else if ("pressure".equalsIgnoreCase(measurementType)) {
      json.put("measurementVal", response.getPressure());
    } else {
      json.put("error", "Unknown measurement type: " + measurementType);
      return 400;
    }

    json.put("timestamp", response.getTimestamp().toEpochMilli());
    return 200;
  }

  @SuppressWarnings("unchecked")
  private int handleLocation(BuoyResponse response, JSONObject json) {
    json.put("buoyId", response.getBuoyId());
    json.put("latitude", response.getLatitude());
    json.put("longitude", response.getLongitude());
    json.put("timestamp", response.getTimestamp().toEpochMilli());
    return 200;
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
