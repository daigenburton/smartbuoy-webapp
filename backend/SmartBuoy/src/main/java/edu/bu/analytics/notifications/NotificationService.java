package edu.bu.analytics.notifications;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a notification generated for a user. Each notification has a type, message, timestamp,
 * and a read/unread status.
 */
public class NotificationService {

  private final List<Notification> notifications = new ArrayList<>();

  public void handleAlert(Alert alert, String userId) {

    // convert alert to notification
    String message;

    if (alert.getAlertType() == Alert.AlertType.DRIFT) {
      message = "Buoy drift detected!";
    } else {
      message = "Entanglement detected!";
    }

    Notification notification =
        new Notification(
            alert.getBuoyId(), userId, alert.getAlertType().toString(), message, Instant.now());

    notifications.add(notification);

    System.out.println("STORED NOTIFICATION: " + notification.toJSON().toJSONString());
  }

  public List<Notification> getNotifications() {
    return notifications;
  }

  public void markAllRead(String userId) {
    for (Notification n : notifications) {
      if (n.getUserId().equals(userId)) {
        n.setRead(true);
      }
    }
  }
}
