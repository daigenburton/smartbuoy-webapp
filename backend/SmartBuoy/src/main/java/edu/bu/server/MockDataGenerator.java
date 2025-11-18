package edu.bu.mock;

import edu.bu.data.BuoyResponse;
import edu.bu.data.InMemoryStore;
import java.util.*;

/**
 * Generates mock buoy data for testing.
 *
 * <p>Creates an InMemoryStore populated with mock data points for multiple buoys, including
 * temperature, pressure, latitude, and longitude.
 */
public class MockDataGenerator {

  public static InMemoryStore generate() {
    InMemoryStore store = new InMemoryStore();
    Random rand = new Random();

    long now = System.currentTimeMillis();

    // Base GPS coordinates for 2 buoys
    double[][] buoyLocations = {
      {42.35, -70.90}, // Buoy 1 near Boston Harbor
      {41.70, -70.00} // Buoy 3 near Cape Cod
    };

    for (int buoyId = 1; buoyId <= 2; buoyId++) {

      double baseLat = buoyLocations[buoyId - 1][0];
      double baseLon = buoyLocations[buoyId - 1][1];

      // Generate 50 data points over past ~48 hours
      for (int i = 0; i < 50; i++) {
        long timestamp = now - (long) (rand.nextDouble() * 48 * 60 * 60 * 1000);

        // Add slight drift to simulate buoy movement
        double lat = baseLat + (rand.nextDouble() - 0.5) * 0.01;
        double lon = baseLon + (rand.nextDouble() - 0.5) * 0.01;

        store.update(
            Arrays.asList(
                new BuoyResponse("temperature", 12 + rand.nextDouble() * 8, buoyId, timestamp),
                new BuoyResponse("pressure", 995 + rand.nextDouble() * 15, buoyId, timestamp),
                new BuoyResponse("latitude", lat, buoyId, timestamp),
                new BuoyResponse("longitude", lon, buoyId, timestamp)));
      }
    }

    return store;
  }

  public static void main(String[] args) {
    generate();
    System.out.println("Mock data generated");
  }
}
