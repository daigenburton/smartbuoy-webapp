package edu.bu.mock;

import edu.bu.data.BuoyResponse;
import edu.bu.data.InMemoryStore;
import java.time.Instant;
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

    // Base GPS coordinates for 3 buoys
    double[][] buoyLocations = {
      {42.35, -70.90}, // Buoy 1 near Boston Harbor
      {41.70, -70.00}, // Buoy 2 near Cape Cod
      {43.10, -70.70} // Buoy 3 near Portsmouth NH
    };

    for (int buoyId = 1; buoyId <= 3; buoyId++) {

      double baseLat = buoyLocations[buoyId - 1][0];
      double baseLon = buoyLocations[buoyId - 1][1];

      // Generate 50 data points over past ~48 hours
      for (int i = 0; i < 50; i++) {
        long timestampMillis = now - (long) (rand.nextDouble() * 48 * 60 * 60 * 1000);
        Instant timestamp = Instant.ofEpochMilli(timestampMillis);

        // Generate sensor readings
        double temperature = 12 + rand.nextDouble() * 8; // 12-20°C
        double pressure = 995 + rand.nextDouble() * 15; // 995-1010 hPa

        // Add slight drift to simulate buoy movement
        double lat = baseLat + (rand.nextDouble() - 0.5) * 0.01;
        double lon = baseLon + (rand.nextDouble() - 0.5) * 0.01;

        // Create single BuoyResponse with all readings
        BuoyResponse response =
            new BuoyResponse(buoyId, timestamp, temperature, pressure, lat, lon);

        store.update(Collections.singletonList(response));
      }
    }

    return store;
  }

  public static void main(String[] args) {
    InMemoryStore store = generate();
    System.out.println("Mock data generated for 3 buoys with 50 readings each");

    // Optional: Print sample data
    try {
      Optional<BuoyResponse> latest = store.getLatest(1);
      if (latest.isPresent()) {
        System.out.println("Sample latest reading for Buoy 1:");
        System.out.println(latest.get().toJSONString());
      }
    } catch (Exception e) {
      System.err.println("Error retrieving sample data: " + e.getMessage());
    }
  }
}
