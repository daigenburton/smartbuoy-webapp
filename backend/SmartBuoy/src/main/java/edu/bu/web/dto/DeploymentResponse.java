package edu.bu.web.dto;

/** Response body for a successful POST /deploy request. */
public class DeploymentResponse {

  private String status;
  private int buoyId;
  private double latitude;
  private double longitude;
  private double allowedRadiusMeters;

  /** Creates a successful deployment response. */
  public DeploymentResponse(
      int buoyId, double latitude, double longitude, double allowedRadiusMeters) {
    this.status = "deployed";
    this.buoyId = buoyId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.allowedRadiusMeters = allowedRadiusMeters;
  }

  public String getStatus() { return status; }
  public int getBuoyId() { return buoyId; }
  public double getLatitude() { return latitude; }
  public double getLongitude() { return longitude; }
  public double getAllowedRadiusMeters() { return allowedRadiusMeters; }
}
