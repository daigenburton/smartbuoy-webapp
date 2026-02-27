package edu.bu.data;

import java.time.Instant;
import org.json.simple.JSONObject;

/** Represents a buoy response containing sensor measurements and metadata */
public class BuoyResponse {

  private final int buoyId;
  private final Instant timestamp;

  // All sensor readings
  private final double temperature; // Celsius
  private final double pressure; // Pascals
  private final double latitude; // GPS latitude
  private final double longitude; // GPS longitude

  public BuoyResponse(
      int buoyId,
      Instant timestamp,
      double temperature,
      double pressure,
      double latitude,
      double longitude) {
    this.buoyId = buoyId;
    this.timestamp = timestamp;
    this.temperature = temperature;
    this.pressure = pressure;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public int getBuoyId() {
    return buoyId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public double getTemperature() {
    return temperature;
  }

  public double getPressure() {
    return pressure;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  /**
   * Converts this BuoyResponse to a JSON object
   *
   * @return JSONObject representation of this buoy response
   */
  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.put("buoyId", buoyId);
    json.put("timestamp", timestamp.toString()); // ISO-8601 format
    json.put("temperature", temperature);
    json.put("pressure", pressure);
    json.put("latitude", latitude);
    json.put("longitude", longitude);
    return json;
  }

  /**
   * Converts this BuoyResponse to a JSON string
   *
   * @return JSON string representation
   */
  public String toJSONString() {
    return toJSON().toJSONString();
  }
}
