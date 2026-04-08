package edu.bu.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** A single entry in a buoy history response, with nullable fields omitted from JSON output. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryEntry {

  private int buoyId;
  private String measurementType;
  private long timestamp;
  private Double temperature;
  private Double pressure;
  private Double latitude;
  private Double longitude;

  /** Creates a temperature history entry. */
  public static HistoryEntry temperature(int buoyId, long timestamp, double temperature) {
    HistoryEntry entry = new HistoryEntry();
    entry.buoyId = buoyId;
    entry.measurementType = "temperature";
    entry.timestamp = timestamp;
    entry.temperature = temperature;
    return entry;
  }

  /** Creates a pressure history entry. */
  public static HistoryEntry pressure(int buoyId, long timestamp, double pressure) {
    HistoryEntry entry = new HistoryEntry();
    entry.buoyId = buoyId;
    entry.measurementType = "pressure";
    entry.timestamp = timestamp;
    entry.pressure = pressure;
    return entry;
  }

  /** Creates a location history entry. */
  public static HistoryEntry location(int buoyId, long timestamp, double latitude, double longitude) {
    HistoryEntry entry = new HistoryEntry();
    entry.buoyId = buoyId;
    entry.measurementType = "location";
    entry.timestamp = timestamp;
    entry.latitude = latitude;
    entry.longitude = longitude;
    return entry;
  }

  public int getBuoyId() { return buoyId; }
  public String getMeasurementType() { return measurementType; }
  public long getTimestamp() { return timestamp; }
  public Double getTemperature() { return temperature; }
  public Double getPressure() { return pressure; }
  public Double getLatitude() { return latitude; }
  public Double getLongitude() { return longitude; }
}
