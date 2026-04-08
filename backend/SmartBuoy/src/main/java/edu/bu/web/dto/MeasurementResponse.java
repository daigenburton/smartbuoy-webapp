package edu.bu.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Response DTO for the latest-measurement endpoints (/temperature, /pressure, /location). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeasurementResponse {

  private int buoyId;
  private String measurementType;
  private long timestamp;
  private Double temperature;
  private Double pressure;
  private Double latitude;
  private Double longitude;

  /** Creates a temperature measurement response. */
  public static MeasurementResponse temperature(int buoyId, long timestamp, double temperature) {
    MeasurementResponse resp = new MeasurementResponse();
    resp.buoyId = buoyId;
    resp.measurementType = "temperature";
    resp.timestamp = timestamp;
    resp.temperature = temperature;
    return resp;
  }

  /** Creates a pressure measurement response. */
  public static MeasurementResponse pressure(int buoyId, long timestamp, double pressure) {
    MeasurementResponse resp = new MeasurementResponse();
    resp.buoyId = buoyId;
    resp.measurementType = "pressure";
    resp.timestamp = timestamp;
    resp.pressure = pressure;
    return resp;
  }

  /** Creates a location measurement response. */
  public static MeasurementResponse location(
      int buoyId, long timestamp, double latitude, double longitude) {
    MeasurementResponse resp = new MeasurementResponse();
    resp.buoyId = buoyId;
    resp.measurementType = "location";
    resp.timestamp = timestamp;
    resp.latitude = latitude;
    resp.longitude = longitude;
    return resp;
  }

  public int getBuoyId() { return buoyId; }
  public String getMeasurementType() { return measurementType; }
  public long getTimestamp() { return timestamp; }
  public Double getTemperature() { return temperature; }
  public Double getPressure() { return pressure; }
  public Double getLatitude() { return latitude; }
  public Double getLongitude() { return longitude; }
}
