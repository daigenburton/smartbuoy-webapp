package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collections;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tinylog.Logger;

/** Handles HTTP requests to the /update endpoint to receive buoy data */
public class UpdateHandler implements HttpHandler {

  final DataStore dataStore;

  public UpdateHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
      exchange.sendResponseHeaders(405, 0);
      exchange.close();
      return;
    }

    try {
      JSONObject requestJson = parseRequestBody(exchange);
      BuoyResponse response = buildBuoyResponse(requestJson);

      dataStore.update(Collections.singletonList(response));
      Logger.info(
          "Received update for buoy {} at {}", response.getBuoyId(), response.getTimestamp());

      sendJsonResponse(exchange, buildSuccessResponse(), 200);

    } catch (Exception e) {
      Logger.error("Error processing update: {}", e.getMessage(), e);
      sendJsonResponse(exchange, buildErrorResponse(e.getMessage()), 400);
    }
  }

  private JSONObject parseRequestBody(HttpExchange exchange) throws Exception {
    return (JSONObject)
        new JSONParser()
            .parse(new BufferedReader(new InputStreamReader(exchange.getRequestBody())));
  }

  private BuoyResponse buildBuoyResponse(JSONObject json) throws Exception {
    // Extract required fields
    int buoyId = ((Long) json.get("buoyId")).intValue();

    // Timestamp - support both long (millis) and ISO string
    Instant timestamp;
    Object timestampObj = json.get("timestamp");
    if (timestampObj instanceof Long) {
      timestamp = Instant.ofEpochMilli((Long) timestampObj);
    } else if (timestampObj instanceof String) {
      timestamp = Instant.parse((String) timestampObj);
    } else {
      timestamp = Instant.now();
    }

    // Extract sensor readings (all required)
    if (!json.containsKey("temperature")
        || !json.containsKey("pressure")
        || !json.containsKey("latitude")
        || !json.containsKey("longitude")) {
      throw new IllegalArgumentException(
          "Missing required fields. Need: temperature, pressure, latitude, longitude");
    }

    double temperature = ((Number) json.get("temperature")).doubleValue();
    double pressure = ((Number) json.get("pressure")).doubleValue();
    double latitude = ((Number) json.get("latitude")).doubleValue();
    double longitude = ((Number) json.get("longitude")).doubleValue();

    return new BuoyResponse(buoyId, timestamp, temperature, pressure, latitude, longitude);
  }

  private JSONObject buildSuccessResponse() {
    JSONObject json = new JSONObject();
    json.put("status", "ok");
    return json;
  }

  private JSONObject buildErrorResponse(String message) {
    JSONObject json = new JSONObject();
    json.put("status", "error");
    json.put("message", message);
    return json;
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
