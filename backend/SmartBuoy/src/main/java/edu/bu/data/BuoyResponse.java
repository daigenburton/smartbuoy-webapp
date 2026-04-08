package edu.bu.data;

import java.time.Instant;

/** Represents a buoy response containing sensor measurements and metadata. */
public class BuoyResponse {

  private final int buoyId;
  private final Instant timestamp;
  private final double temperature;
  private final double pressure;
  private final double latitude;
  private final double longitude;

  /** Creates a BuoyResponse with all sensor readings. */
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

  public int getBuoyId() { return buoyId; }
  public Instant getTimestamp() { return timestamp; }
  public double getTemperature() { return temperature; }
  public double getPressure() { return pressure; }
  public double getLatitude() { return latitude; }
  public double getLongitude() { return longitude; }
}
