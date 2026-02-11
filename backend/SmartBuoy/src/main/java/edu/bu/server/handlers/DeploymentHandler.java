package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import edu.bu.data.Deployment;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tinylog.Logger;

/**
 * HTTP handler for buoy deployment requests.
 *
 * <p>Handles POST requests to deploy a buoy by: Retrieving the buoy's most recent GPS coordinates
 * from DataStore, Creating a deployment with the specified geofence radius, & Saving the deployment
 * configuration for future monitoring
 *
 * <p>Returns an error if the buoy has not yet reported GPS data or if the buoy ID is unknown.
 */
public class DeploymentHandler implements HttpHandler {

  private final DataStore dataStore;

  public DeploymentHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  private void addCorsHeaders(HttpExchange exchange) {
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    addCorsHeaders(exchange);

    if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
      exchange.sendResponseHeaders(204, -1);
      return;
    }

    if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
      sendErrorResponse(exchange, "Method not allowed", 405);
      return;
    }

    try {
      processDeploymentRequest(exchange);
    } catch (ParseException | NullPointerException | ClassCastException e) {
      sendErrorResponse(exchange, "Invalid deployment request", 400);
    }
  }

  private void processDeploymentRequest(HttpExchange exchange) throws IOException, ParseException {
    JSONObject request = parseRequestBody(exchange);
    int buoyId = ((Number) request.get("buoyId")).intValue();

    Optional<double[]> coordinatesOpt = retrieveBuoyCoordinates(exchange, buoyId);
    if (!coordinatesOpt.isPresent()) {
      return; // Error response already sent
    }

    double[] coordinates = coordinatesOpt.get();
    double lat = coordinates[0];
    double lon = coordinates[1];
    double allowedRadius = ((Number) request.get("allowedRadiusMeters")).doubleValue();

    Deployment deployment =
        new Deployment(buoyId, lat, lon, allowedRadius, System.currentTimeMillis());
    dataStore.saveDeployment(deployment);

    sendSuccessResponse(exchange, buoyId, lat, lon, allowedRadius);
    Logger.info("Deployment data saved for buoy {}", buoyId);
  }

  private JSONObject parseRequestBody(HttpExchange exchange) throws IOException, ParseException {
    try (InputStream inputStream = exchange.getRequestBody()) {
      String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      JSONParser parser = new JSONParser();
      return (JSONObject) parser.parse(body);
    }
  }

  private Optional<double[]> retrieveBuoyCoordinates(HttpExchange exchange, int buoyId)
      throws IOException {
    try {
      BuoyResponse latReading = dataStore.getLatest(buoyId, "latitude").orElse(null);
      BuoyResponse lonReading = dataStore.getLatest(buoyId, "longitude").orElse(null);

      if (latReading == null || lonReading == null) {
        sendErrorResponse(
            exchange,
            "Device has not reported GPS data yet. Please place buoy in water and wait for signal.",
            400);
        return Optional.empty();
      }

      return Optional.of(new double[] {latReading.measurementVal, lonReading.measurementVal});

    } catch (UnknownBuoyException e) {
      sendErrorResponse(
          exchange,
          "Buoy "
              + buoyId
              + " has not sent any data yet. Please ensure the buoy is active and transmitting before deployment.",
          404);
      return Optional.empty();
    }
  }

  private void sendSuccessResponse(
      HttpExchange exchange, int buoyId, double lat, double lon, double allowedRadius)
      throws IOException {
    JSONObject response = new JSONObject();
    response.put("status", "deployed");
    response.put("buoyId", buoyId);
    response.put("latitude", lat);
    response.put("longitude", lon);
    response.put("allowedRadiusMeters", allowedRadius);
    sendJsonResponse(exchange, response, 200);
  }

  private void sendErrorResponse(HttpExchange exchange, String errorMessage, int statusCode)
      throws IOException {
    JSONObject response = new JSONObject();
    response.put("error", errorMessage);
    sendJsonResponse(exchange, response, statusCode);
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
