package edu.bu.web.dto;

/** Request body for the POST /deploy endpoint. */
public class DeploymentRequest {

  private int buoyId;
  private double allowedRadiusMeters;

  public int getBuoyId() { return buoyId; }
  public void setBuoyId(int buoyId) { this.buoyId = buoyId; }
  public double getAllowedRadiusMeters() { return allowedRadiusMeters; }
  public void setAllowedRadiusMeters(double allowedRadiusMeters) { this.allowedRadiusMeters = allowedRadiusMeters; }
}
