package edu.bu.analytics.geofence;

import edu.bu.data.Deployment;

/** Determines if the current location is inside/outside configured geofence. */
public class GeofenceService {

  public static boolean isOutsideFence(
      Deployment deployment, double currentLat, double currentLon) {

    double distance =
        GeoUtils.distanceMeters(deployment.lat, deployment.lon, currentLat, currentLon);

    return distance > deployment.allowedRadiusMeters;
  }
}
