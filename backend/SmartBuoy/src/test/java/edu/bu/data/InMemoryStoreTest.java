package edu.bu.data;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.analytics.UnknownBuoyException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryStoreTest {

  private InMemoryStore store;

  @BeforeEach
  public void setUp() {
    store = new InMemoryStore();
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

    // Retrieve and verify
    List<BuoyResponse> history = store.getHistory(1);
    assertEquals(2, history.size());
    assertEquals(20.5, history.get(0).getTemperature());
    assertEquals(21.0, history.get(1).getTemperature());
  }

  /* Update two buoys */
  @Test
  public void testMultipleBuoys() throws UnknownBuoyException {
    BuoyResponse buoy1Response = new BuoyResponse(1, Instant.now(), 20.5, 101325.0, 42.36, -71.05);
    BuoyResponse buoy2Response = new BuoyResponse(2, Instant.now(), 22.0, 101320.0, 41.70, -70.00);

    store.update(Arrays.asList(buoy1Response, buoy2Response));

    assertEquals(1, store.getHistory(1).size());
    assertEquals(1, store.getHistory(2).size());
    assertEquals(20.5, store.getHistory(1).get(0).getTemperature());
    assertEquals(22.0, store.getHistory(2).get(0).getTemperature());
  }

  /* Update nonexisting buoy */
  @Test
  public void testUnknownBuoyThrowsException() {
    assertThrows(
        UnknownBuoyException.class,
        () -> {
          store.getHistory(999);
        });
  }

  /* Update buoy with null data in list */
  @Test
  public void testNullResponseHandling() throws UnknownBuoyException {
    BuoyResponse response1 = new BuoyResponse(1, Instant.now(), 20.5, 101325.0, 42.36, -71.05);

    store.update(Arrays.asList(response1, null));

    List<BuoyResponse> history = store.getHistory(1);
    assertEquals(1, history.size()); // null should be skipped
  }

  @Test
  public void testReturnedListIsIndependent() throws UnknownBuoyException {
    BuoyResponse response = new BuoyResponse(1, Instant.now(), 20.5, 101325.0, 42.36, -71.05);
    store.update(Arrays.asList(response));

    List<BuoyResponse> history1 = store.getHistory(1);
    List<BuoyResponse> history2 = store.getHistory(1);

    assertNotSame(history1, history2);
  }

  @Test
  public void testOldDataIsDiscarded() throws Exception {
    Instant now = Instant.now();
    Instant eightDaysAgo = now.minus(8, ChronoUnit.DAYS);

    BuoyResponse old = new BuoyResponse(1, eightDaysAgo, 10.0, 101325.0, 42.36, -71.05);
    BuoyResponse fresh = new BuoyResponse(1, now, 20.0, 101320.0, 42.36, -71.05);

    store.update(Arrays.asList(old, fresh));

    List<BuoyResponse> history = store.getHistory(1);

    assertEquals(1, history.size());
    assertEquals(20.0, history.get(0).getTemperature());
  }

  @Test
  public void testGetLatestReturnsMostRecent() throws Exception {
    Instant oldTs = Instant.now();
    Instant newTs = oldTs.plusSeconds(5);

    BuoyResponse oldR = new BuoyResponse(1, oldTs, 20.5, 101325.0, 42.36, -71.05);
    BuoyResponse newR = new BuoyResponse(1, newTs, 21.0, 101320.0, 42.36, -71.05);

    store.update(Arrays.asList(oldR, newR));

    Optional<BuoyResponse> latest = store.getLatest(1);
    assertTrue(latest.isPresent());
    assertEquals(21.0, latest.get().getTemperature());
    assertEquals(newTs, latest.get().getTimestamp());
  }

  @Test
  public void testGetLatestReturnsCompleteReading() throws Exception {
    BuoyResponse response = new BuoyResponse(1, Instant.now(), 20.5, 101325.0, 42.36, -71.05);

    store.update(Arrays.asList(response));

    Optional<BuoyResponse> latest = store.getLatest(1);

    assertTrue(latest.isPresent());
    assertEquals(20.5, latest.get().getTemperature());
    assertEquals(101325.0, latest.get().getPressure());
    assertEquals(42.36, latest.get().getLatitude());
    assertEquals(-71.05, latest.get().getLongitude());
  }

  @Test
  public void testGetLatestReturnsEmptyForUnknownBuoy() throws Exception {
    BuoyResponse response = new BuoyResponse(1, Instant.now(), 20.5, 101325.0, 42.36, -71.05);
    store.update(List.of(response));

    assertThrows(UnknownBuoyException.class, () -> store.getLatest(999));
  }

  @Test
  public void testGetLatestUnknownBuoyThrowsException() {
    assertThrows(UnknownBuoyException.class, () -> store.getLatest(999));
  }

  @Test
  public void testMultipleReadingsPreserveAllData() throws Exception {
    Instant time1 = Instant.now();
    Instant time2 = time1.plusSeconds(30);
    Instant time3 = time2.plusSeconds(30);

    BuoyResponse r1 = new BuoyResponse(1, time1, 20.0, 101325.0, 42.36, -71.05);
    BuoyResponse r2 = new BuoyResponse(1, time2, 20.5, 101320.0, 42.37, -71.06);
    BuoyResponse r3 = new BuoyResponse(1, time3, 21.0, 101315.0, 42.38, -71.07);

    store.update(Arrays.asList(r1, r2, r3));

    List<BuoyResponse> history = store.getHistory(1);
    assertEquals(3, history.size());

    // Verify all measurements are preserved
    assertEquals(20.0, history.get(0).getTemperature());
    assertEquals(101325.0, history.get(0).getPressure());
    assertEquals(42.36, history.get(0).getLatitude());

    assertEquals(21.0, history.get(2).getTemperature());
    assertEquals(101315.0, history.get(2).getPressure());
    assertEquals(42.38, history.get(2).getLatitude());
  }
}
