package edu.bu.analytics.notifications;

import java.time.Instant;
import org.json.simple.JSONObject;

/**
 * Represents a notification generated for a buoy.
 *
 * <p>Each notification has a type, message, timestamp, and a read/unread status.
 */
public class Notification {

  private final int buoyId;
  private final String userId;
  private final String type;
  private final String message;
  private final Instant timestamp;
  private boolean read;

  public Notification(int buoyId, String userId, String type, String message, Instant timestamp) {
    this.buoyId = buoyId;
    this.userId = userId;
    this.type = type;
    this.message = message;
    this.timestamp = timestamp;
    this.read = false;
  }

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.put("userId", userId);
    json.put("buoyId", buoyId);
    json.put("type", type);
    json.put("message", message);
    json.put("timestamp", timestamp.toString());
    json.put("read", read);
    return json;
  }

  public String getUserId() {
    return userId;
  }

  public void setRead(boolean read) {
    this.read = read;
  }
}
