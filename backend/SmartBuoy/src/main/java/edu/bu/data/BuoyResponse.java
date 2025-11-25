package edu.bu.data;

import java.time.Instant;

/** Represents a buoy response containing sensor measurements and metadata */
public class BuoyResponse {

  public final int buoyId;
  public final double measurementVal;
  public final String measurementType; // temp, tide level, salinity, location
  public final long timestamp; // ms

  public BuoyResponse(String measurementType, double measurementVal, int buoyId, long timestamp) {
    this.buoyId = buoyId;
    this.measurementVal = measurementVal;
    this.measurementType = measurementType;
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "BuoyResponse{"
        + "buoyId='"
        + buoyId
        + '\''
        + ", measurementType="
        + measurementType
        + ", time="
        + Instant.ofEpochMilli(timestamp)
        + ", measurementVal="
        + measurementVal
        + '}';
  }
}
