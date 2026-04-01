package edu.bu.analytics.notifications;

import java.time.Instant;
import org.json.simple.JSONObject;

/** Represents an alert generated for a buoy event. */
public class Alert {

  /** Events that generate alerts */
  public enum AlertType {

    /** Indicates that a buoy has left its allowed deployment area (geofence). */
    DRIFT,

    /** Indicates wildlife entaglement has been detected. */
    ENTANGLEMENT
  }

  private final int buoyId;
  private final AlertType alertType;
  private final Instant timestamp;
  private final double latitude;
  private final double longitude;
  private final String severity;

  public Alert(
      int buoyId,
      AlertType alertType,
      Instant timestamp,
      double latitude,
      double longitude,
      String severity) {
    this.buoyId = buoyId;
    this.alertType = alertType;
    this.timestamp = timestamp;
    this.latitude = latitude;
    this.longitude = longitude;
    this.severity = severity;
  }

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.put("buoyId", buoyId);
    json.put("alertType", alertType.toString());
    json.put("timestamp", timestamp.toString());
    json.put("latitude", latitude);
    json.put("longitude", longitude);
    json.put("severity", severity);
    return json;
  }

  public int getBuoyId() {
    return buoyId;
  }

  public AlertType getAlertType() {
    return alertType;
  }
}
