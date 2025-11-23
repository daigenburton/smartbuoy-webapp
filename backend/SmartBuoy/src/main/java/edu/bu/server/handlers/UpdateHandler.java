package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
      int buoyId = ((Long) requestJson.get("buoyId")).intValue();
      long timestamp = (Long) requestJson.get("timestamp");
      List<BuoyResponse> responses = buildBuoyResponse(requestJson, buoyId, timestamp);

      dataStore.update(responses);
      Logger.info("Received update for buoy {} with {} measurements", buoyId, responses.size());

      sendJsonResponse(exchange, buildSuccessResponse(), 200);

    } catch (Exception e) {
      Logger.error("Error processing update: {}", e.getMessage(), e);
      exchange.sendResponseHeaders(400, 0);
      exchange.close();
    }
  }

  private JSONObject parseRequestBody(HttpExchange exchange) throws Exception {
    return (JSONObject)
        new JSONParser()
            .parse(new BufferedReader(new InputStreamReader(exchange.getRequestBody())));
  }

  private List<BuoyResponse> buildBuoyResponse(JSONObject json, int buoyId, long timestamp) {
    List<BuoyResponse> responses = new ArrayList<>();

    if (json.containsKey("temperature")) {
      responses.add(
          new BuoyResponse(
              "temperature", ((Number) json.get("temperature")).doubleValue(), buoyId, timestamp));
    }
    if (json.containsKey("pressure")) {
      responses.add(
          new BuoyResponse(
              "pressure", ((Number) json.get("pressure")).doubleValue(), buoyId, timestamp));
    }
    if (json.containsKey("latitude")) {
      responses.add(
          new BuoyResponse(
              "latitude", ((Number) json.get("latitude")).doubleValue(), buoyId, timestamp));
    }
    if (json.containsKey("longitude")) {
      responses.add(
          new BuoyResponse(
              "longitude", ((Number) json.get("longitude")).doubleValue(), buoyId, timestamp));
    }

    return responses;
  }

  private JSONObject buildSuccessResponse() {
    JSONObject json = new JSONObject();
    json.put("status", "ok");
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
