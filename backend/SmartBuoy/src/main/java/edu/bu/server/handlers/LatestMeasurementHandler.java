// package edu.bu.server.handlers;

// import com.sun.net.httpserver.HttpExchange;
// import com.sun.net.httpserver.HttpHandler;
// import edu.bu.analytics.UnknownBuoyException;
// import edu.bu.data.BuoyResponse;
// import edu.bu.data.DataStore;
// import java.io.IOException;
// import java.io.OutputStream;
// import java.util.Optional;
// import org.json.simple.JSONObject;
// import org.tinylog.Logger;

// /**
//  * Handles requests like /temp/{id}, /pressure/{id}, /location/{id} and returns the latest
//  * measurement.
//  */
// public class LatestMeasurementHandler implements HttpHandler {

//   final DataStore dataStore;
//   final String measurementType;

//   public LatestMeasurementHandler(DataStore dataStore, String measurementType) {
//     this.dataStore = dataStore;
//     this.measurementType = measurementType;
//   }

//   @Override
//   public void handle(HttpExchange exchange) throws IOException {

//     JSONObject json = new JSONObject();
//     int statusCode;

//     try {
//       int buoyId = Integer.parseInt(getBuoyIdFromPath(exchange));

//       if ("location".equalsIgnoreCase(measurementType)) {
//         statusCode = handleLocation(buoyId, json);
//       } else {
//         Optional<BuoyResponse> latest = dataStore.getLatest(buoyId, measurementType);

//         if (latest.isPresent()) {
//           fillJsonResponse(json, latest.get());
//           statusCode = 200;
//         } else {
//           json.put("error", "No " + measurementType + " data for buoy " + buoyId);
//           statusCode = 404;
//         }
//       }
//     } catch (UnknownBuoyException e) {
//       json.put("error", e.getMessage());
//       statusCode = 404;
//     }

//     sendJsonResponse(exchange, json, statusCode);
//     Logger.info(
//         "Handled latest {} request for buoy {}, responding with {}.",
//         measurementType,
//         getBuoyIdFromPath(exchange),
//         json.toString());
//   }

//   private String getBuoyIdFromPath(HttpExchange exchange) {
//     String[] parts = exchange.getRequestURI().getRawPath().split("/");
//     return parts[parts.length - 1];
//   }

//   private int handleLocation(int buoyId, JSONObject json) throws UnknownBuoyException {

//     Optional<BuoyResponse> latOpt = dataStore.getLatest(buoyId, "latitude");
//     Optional<BuoyResponse> lonOpt = dataStore.getLatest(buoyId, "longitude");

//     if (latOpt.isPresent() && lonOpt.isPresent()) {
//       json.put("buoyId", buoyId);
//       json.put("latitude", latOpt.get().measurementVal);
//       json.put("longitude", lonOpt.get().measurementVal);
//       long latestTimestamp = Math.max(latOpt.get().msSinceEpoch, lonOpt.get().msSinceEpoch);
//       json.put("timestamp", latestTimestamp);
//       return 200;
//     } else {
//       json.put("error", "No location data for buoy " + buoyId);
//       return 404;
//     }
//   }

//   private void fillJsonResponse(JSONObject json, BuoyResponse response) {
//     json.put("buoyId", response.buoyId);
//     json.put("measurementType", response.measurementType);
//     json.put("measurementVal", response.measurementVal);
//     json.put("timestamp", response.msSinceEpoch);
//   }

//   private void sendJsonResponse(HttpExchange exchange, JSONObject json, int statusCode)
//       throws IOException {
//     String response = json.toString();
//     exchange.getResponseHeaders().set("Content-Type", "application/json");
//     exchange.sendResponseHeaders(statusCode, response.length());
//     try (OutputStream outputStream = exchange.getResponseBody()) {
//       outputStream.write(response.getBytes());
//     }
//   }
// }
package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import org.json.simple.JSONObject;
import org.tinylog.Logger;

/**
 * Handles requests like /temperature/{id}, /pressure/{id}, /location/{id} and returns the latest
 * (simulated live) measurement.
 *
 * <p>For mock/demo mode: - Each request slightly perturbs the last known reading and stores a new
 * one, so values change over time when the frontend refreshes.
 */
public class LatestMeasurementHandler implements HttpHandler {

  final DataStore dataStore;
  final String measurementType;
  private final Random rand = new Random();

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
        statusCode = handleScalarMeasurement(buoyId, json);
      }

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

  private String getBuoyIdFromPath(HttpExchange exchange) {
    String[] parts = exchange.getRequestURI().getRawPath().split("/");
    return parts[parts.length - 1];
  }

  /**
   * Handle non-location scalar measurements like temperature or pressure.
   *
   * <p>For mock data, we: - get the latest reading - create a slightly jittered new reading - store
   * it - return the new reading
   */
  private int handleScalarMeasurement(int buoyId, JSONObject json) throws UnknownBuoyException {

    Optional<BuoyResponse> latestOpt = dataStore.getLatest(buoyId, measurementType);

    if (latestOpt.isEmpty()) {
      json.put("error", "No " + measurementType + " data for buoy " + buoyId);
      return 404;
    }

    BuoyResponse latest = latestOpt.get();

    // Small random change to simulate new reading
    double jitter = createJitterForMeasurement(measurementType);
    double newValue = latest.measurementVal + jitter;

    BuoyResponse newReading =
        new BuoyResponse(
            latest.measurementType, newValue, latest.buoyId, System.currentTimeMillis());

    // Store new reading so "latest" moves forward over time
    dataStore.update(Arrays.asList(newReading));

    fillJsonResponse(json, newReading);
    return 200;
  }

  /**
   * Handle location requests by jittering latitude/longitude slightly and storing a new "latest"
   * position.
   */
  private int handleLocation(int buoyId, JSONObject json) throws UnknownBuoyException {

    Optional<BuoyResponse> latOpt = dataStore.getLatest(buoyId, "latitude");
    Optional<BuoyResponse> lonOpt = dataStore.getLatest(buoyId, "longitude");

    if (latOpt.isEmpty() || lonOpt.isEmpty()) {
      json.put("error", "No location data for buoy " + buoyId);
      return 404;
    }

    BuoyResponse latestLat = latOpt.get(), latestLon = lonOpt.get();

    // Jitter coordinates slightly (very small drift)
    double latJitter = (rand.nextDouble() - 0.5) * 0.0005; // ~±0.00025 deg
    double lonJitter = (rand.nextDouble() - 0.5) * 0.0005;

    double newLat = latestLat.measurementVal + latJitter;
    double newLon = latestLon.measurementVal + lonJitter;

    BuoyResponse newLatResp =
        new BuoyResponse("latitude", newLat, buoyId, System.currentTimeMillis());
    BuoyResponse newLonResp =
        new BuoyResponse("longitude", newLon, buoyId, System.currentTimeMillis());

    // Store updated location
    dataStore.update(Arrays.asList(newLatResp, newLonResp));

    json.put("buoyId", buoyId);
    json.put("latitude", newLat);
    json.put("longitude", newLon);
    json.put("timestamp", System.currentTimeMillis());

    return 200;
  }

  /** Decide how much to jitter each measurement type */
  private double createJitterForMeasurement(String measurementType) {
    if ("temperature".equalsIgnoreCase(measurementType)) {
      // e.g., ±0.2 degrees
      return (rand.nextDouble() - 0.5) * 0.4;
    } else if ("pressure".equalsIgnoreCase(measurementType)) {
      // e.g., ±0.5 hPa
      return (rand.nextDouble() - 0.5) * 1.0;
    }
    // default small jitter
    return (rand.nextDouble() - 0.5) * 0.2;
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
