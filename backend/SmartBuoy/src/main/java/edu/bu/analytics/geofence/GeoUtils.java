package edu.bu.analytics.geofence;

/**
 * Utility class for geographic calculations. Provides method for computing distances between
 * geographic coordinates.
 */
public class GeoUtils {
  private static final double EARTH_RADIUS = 6371000;

  public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double haversineA =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    return 2 * EARTH_RADIUS * Math.atan2(Math.sqrt(haversineA), Math.sqrt(1 - haversineA));
  }
}
