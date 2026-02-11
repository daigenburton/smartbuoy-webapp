package edu.bu.analytics.geofence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GeoUtilsTest {

  @Test
  public void testDistanceSamePointIsZero() {
    double dist = GeoUtils.distanceMeters(42.0, -70.0, 42.0, -70.0);

    assertEquals(0.0, dist, 0.01);
  }

  @Test
  public void testKnownDistanceRoughlyCorrect() {
    // Boston → Cambridge ≈ 5km
    double dist =
        GeoUtils.distanceMeters(
            42.3601, -71.0589,
            42.3736, -71.1097);

    assertTrue(dist > 4000);
    assertTrue(dist < 6000);
  }
}
