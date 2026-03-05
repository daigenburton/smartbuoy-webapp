package edu.bu.analytics.geofence;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.data.Deployment;
import org.junit.jupiter.api.Test;

public class GeofenceServiceTest {

  @Test
  public void testInsideFence() {
    Deployment d = new Deployment(1, 42.0, -70.0, 1000, System.currentTimeMillis());

    boolean outside = GeofenceService.isOutsideFence(d, 42.0001, -70.0001);

    assertFalse(outside);
  }

  @Test
  public void testOutsideFence() {
    Deployment d = new Deployment(1, 42.0, -70.0, 50, System.currentTimeMillis());

    boolean outside = GeofenceService.isOutsideFence(d, 42.01, -70.01);

    assertTrue(outside);
  }
}
