package edu.bu.data;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.analytics.UnknownBuoyException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Test")
public class InfluxDBTest {

  private InfluxDBStore store;

  @BeforeEach
  public void setUp() {
    store = new InfluxDBStore();
  }

  @AfterEach
  public void tearDown() {
    store.close();
  }

  /* Update the store with measurements for the same buoy */
  @Test
  public void testUpdateAndRetrieve() throws UnknownBuoyException {
    // Create test data
    BuoyResponse response1 = new BuoyResponse(1, Instant.now(), 20.5, 101325.0, 42.36, -71.05);
    BuoyResponse response2 =
        new BuoyResponse(1, Instant.now().plusSeconds(60), 21.0, 101320.0, 42.37, -71.06);

    // Update store
    store.update(Arrays.asList(response1, response2));

    // Wait for data to be indexed
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Retrieve and verify
    List<BuoyResponse> history = store.getHistory(1);
    assertTrue(history.size() >= 2);

    // Verify data contains our values
    boolean foundFirst = history.stream().anyMatch(r -> Math.abs(r.getTemperature() - 20.5) < 0.01);
    boolean foundSecond =
        history.stream().anyMatch(r -> Math.abs(r.getTemperature() - 21.0) < 0.01);

    assertTrue(foundFirst);
    assertTrue(foundSecond);
  }

  /* Update two buoys */
  @Test
  public void testMultipleBuoys() throws UnknownBuoyException {
    BuoyResponse buoy1Response = new BuoyResponse(10, Instant.now(), 20.5, 101325.0, 42.36, -71.05);
    BuoyResponse buoy2Response = new BuoyResponse(20, Instant.now(), 22.0, 101320.0, 41.70, -70.00);

    store.update(Arrays.asList(buoy1Response, buoy2Response));

    // Wait for data to be indexed
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    List<BuoyResponse> history1 = store.getHistory(10);
    List<BuoyResponse> history2 = store.getHistory(20);

    assertTrue(history1.size() >= 1);
    assertTrue(history2.size() >= 1);

    assertEquals(20.5, history1.get(history1.size() - 1).getTemperature(), 0.01);
    assertEquals(22.0, history2.get(history2.size() - 1).getTemperature(), 0.01);
  }

  /* Update nonexistent buoy */
  @Test
  public void testUnknownBuoyThrowsException() {
    assertThrows(
        UnknownBuoyException.class,
        () -> {
          store.getHistory(999999);
        });
  }

  @Test
  public void testGetLatestReturnsMostRecent() throws Exception {
    Instant oldTs = Instant.now().minusSeconds(60); // 1 minute ago
    Instant newTs = Instant.now(); // now

    BuoyResponse oldR = new BuoyResponse(30, oldTs, 20.5, 101325.0, 42.36, -71.05);
    BuoyResponse newR = new BuoyResponse(30, newTs, 21.0, 101320.0, 42.36, -71.05);

    store.update(Arrays.asList(oldR, newR));

    // Wait for data to be indexed
    Thread.sleep(2000); // Increase wait time

    Optional<BuoyResponse> latest = store.getLatest(30);
    assertTrue(latest.isPresent());

    // Debug: print what we got
    System.out.println("Expected: 21.0, Got: " + latest.get().getTemperature());
    System.out.println("Timestamp: " + latest.get().getTimestamp());

    assertEquals(21.0, latest.get().getTemperature(), 0.01);
  }

  @Test
  public void testGetLatestReturnsCompleteReading() throws Exception {
    BuoyResponse response = new BuoyResponse(40, Instant.now(), 20.5, 101325.0, 42.36, -71.05);

    store.update(Arrays.asList(response));

    // Wait for data to be indexed
    Thread.sleep(1000);

    Optional<BuoyResponse> latest = store.getLatest(40);

    assertTrue(latest.isPresent());
    assertEquals(20.5, latest.get().getTemperature(), 0.01);
    assertEquals(101325.0, latest.get().getPressure(), 0.01);
    assertEquals(42.36, latest.get().getLatitude(), 0.01);
    assertEquals(-71.05, latest.get().getLongitude(), 0.01);
  }

  @Test
  public void testGetLatestReturnsEmptyForUnknownBuoy() throws Exception {
    Optional<BuoyResponse> latest = store.getLatest(888888);
    assertTrue(latest.isEmpty());
  }

  @Test
  public void testMultipleReadingsPreserveAllData() throws Exception {
    Instant time1 = Instant.now().minusSeconds(120); // 2 minutes ago
    Instant time2 = Instant.now().minusSeconds(60); // 1 minute ago
    Instant time3 = Instant.now(); // now

    BuoyResponse r1 = new BuoyResponse(50, time1, 20.0, 101325.0, 42.36, -71.05);
    BuoyResponse r2 = new BuoyResponse(50, time2, 20.5, 101320.0, 42.37, -71.06);
    BuoyResponse r3 = new BuoyResponse(50, time3, 21.0, 101315.0, 42.38, -71.07);

    store.update(Arrays.asList(r1, r2, r3));

    // Wait for data to be indexed
    Thread.sleep(2000);

    List<BuoyResponse> history = store.getHistory(50);
    assertTrue(history.size() >= 3);

    // Verify all measurements are preserved
    boolean found20 = history.stream().anyMatch(r -> Math.abs(r.getTemperature() - 20.0) < 0.01);
    boolean found205 = history.stream().anyMatch(r -> Math.abs(r.getTemperature() - 20.5) < 0.01);
    boolean found21 = history.stream().anyMatch(r -> Math.abs(r.getTemperature() - 21.0) < 0.01);

    assertTrue(found20);
    assertTrue(found205);
    assertTrue(found21);
  }

  @Test
  public void testAllSensorFieldsStored() throws Exception {
    BuoyResponse response = new BuoyResponse(60, Instant.now(), 22.5, 101325.0, 42.3601, -71.0589);

    store.update(Arrays.asList(response));

    // Wait for data to be indexed
    Thread.sleep(1000);

    Optional<BuoyResponse> latest = store.getLatest(60);
    assertTrue(latest.isPresent());

    BuoyResponse retrieved = latest.get();
    assertEquals(60, retrieved.getBuoyId());
    assertEquals(22.5, retrieved.getTemperature(), 0.01);
    assertEquals(101325.0, retrieved.getPressure(), 0.01);
    assertEquals(42.3601, retrieved.getLatitude(), 0.0001);
    assertEquals(-71.0589, retrieved.getLongitude(), 0.0001);
  }

  @Test
  public void testHistorySortedByTime() throws Exception {
    Instant now = Instant.now();

    // Insert out of order
    BuoyResponse r3 = new BuoyResponse(70, now, 21.0, 101315.0, 42.38, -71.07);
    BuoyResponse r1 = new BuoyResponse(70, now.minusSeconds(120), 20.0, 101325.0, 42.36, -71.05);
    BuoyResponse r2 = new BuoyResponse(70, now.minusSeconds(60), 20.5, 101320.0, 42.37, -71.06);

    store.update(Arrays.asList(r3, r1, r2));

    // Wait for data to be indexed
    Thread.sleep(2000);

    List<BuoyResponse> history = store.getHistory(70);
    assertTrue(history.size() >= 3);

    // Verify sorted by time (ascending)
    for (int i = 0; i < history.size() - 1; i++) {
      assertTrue(
          history.get(i).getTimestamp().isBefore(history.get(i + 1).getTimestamp())
              || history.get(i).getTimestamp().equals(history.get(i + 1).getTimestamp()));
    }
  }
}
