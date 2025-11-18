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

      if ("location".equalsIgnoreCase(measurementType)) {
        statusCode = handleLocation(buoyId, json);
      } else {
        Optional<BuoyResponse> latest = dataStore.getLatest(buoyId, measurementType);

        if (latest.isPresent()) {
          fillJsonResponse(json, latest.get());
          statusCode = 200;
        } else {
          json.put("error", "No " + measurementType + " data for buoy " + buoyId);
          statusCode = 404;
        }
      }
    } catch (UnknownBuoyException e) {
      json.put("error", e.getMessage());
      statusCode = 404;
    }

    sendJsonResponse(exchange, json, statusCode);
    Logger.info(
        "Handled latest {} request for buoy {}, responding with {}.",
        measurementType,
        getBuoyIdFromPath(exchange),
        json.toString());
  }

  private String getBuoyIdFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getRawPath().split("/");
    return parts[parts.length - 1];
  }

  private int handleLocation(int buoyId, JSONObject json) throws UnknownBuoyException {

    Optional<BuoyResponse> latOpt = dataStore.getLatest(buoyId, "latitude");
    Optional<BuoyResponse> lonOpt = dataStore.getLatest(buoyId, "longitude");

    if (latOpt.isPresent() && lonOpt.isPresent()) {
      json.put("buoyId", buoyId);
      json.put("latitude", latOpt.get().measurementVal);
      json.put("longitude", lonOpt.get().measurementVal);
      long latestTimestamp = Math.max(latOpt.get().msSinceEpoch, lonOpt.get().msSinceEpoch);
      json.put("timestamp", latestTimestamp);
      return 200;
    } else {
      json.put("error", "No location data for buoy " + buoyId);
      return 404;
    }
  }

  private void fillJsonResponse(JSONObject json, BuoyResponse response) {
    json.put("buoyId", response.buoyId);
    json.put("measurementType", response.measurementType);
    json.put("value", response.measurementVal);
    json.put("timestamp", response.msSinceEpoch);
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
