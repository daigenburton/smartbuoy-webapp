package edu.bu.data;

import java.time.Instant;

/** Represents a buoy response containing sensor measurements and metadata */
public class BuoyResponse {

  public final int buoyId;
  public final double measurementVal;
  public final String measurementType; // temp, tide level, salinity, location
  public final long msSinceEpoch; // timestamp

  public BuoyResponse(
      String measurementType, double measurementVal, int buoyId, long msSinceEpoch) {
    this.buoyId = buoyId;
    this.measurementVal = measurementVal;
    this.measurementType = measurementType;
    this.msSinceEpoch = msSinceEpoch;
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
        + Instant.ofEpochMilli(msSinceEpoch)
        + ", measurementVal="
        + measurementVal
        + '}';
  }
}
