package edu.bu.data;

/**
 * Represents a buoy deployment configuration.
 *
 * <p>A deployment defines the geographic boundaries within which a buoy is expected to remain.
 */
public class Deployment {
  public final int buoyId;
  public final double lat;
  public final double lon;
  public final double allowedRadiusMeters;
  public final long deployedAt;

  public Deployment(
      int buoyId, double lat, double lon, double allowedRadiusMeters, long deployedAt) {
    this.buoyId = buoyId;
    this.lat = lat;
    this.lon = lon;
    this.allowedRadiusMeters = allowedRadiusMeters;
    this.deployedAt = deployedAt;
  }
}
