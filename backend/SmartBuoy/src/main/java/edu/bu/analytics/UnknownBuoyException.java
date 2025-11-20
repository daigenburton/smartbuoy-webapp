package edu.bu.analytics;

/**
 * Thrown when SmartBuoy is asked a question about a buoyId that it has not been registered to
 * follow.
 */
public class UnknownBuoyException extends Exception {
  final int buoy_id;

  public UnknownBuoyException(int buoy_id) {
    this.buoy_id = buoy_id;
  }

  @Override
  public String getMessage() {
    return "Buoy " + buoy_id + " has not been seen by the server";
  }
}
