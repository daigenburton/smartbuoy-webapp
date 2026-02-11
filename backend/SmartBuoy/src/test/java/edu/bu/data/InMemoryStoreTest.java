package edu.bu.data;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.analytics.UnknownBuoyException;
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

  /* Update the store with two measurements for the same buoy */
  @Test
  public void testUpdateAndRetrieve() throws UnknownBuoyException {
    // Create test data
    BuoyResponse response1 = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
    BuoyResponse response2 = new BuoyResponse("salinity", 35.0, 1, System.currentTimeMillis());

    // Update store
    store.update(Arrays.asList(response1, response2));

    // Retrieve and verify
    List<BuoyResponse> history = store.getHistory(1);
    assertEquals(2, history.size());
    assertEquals(20.5, history.get(0).measurementVal);
    assertEquals(35.0, history.get(1).measurementVal);
  }

  /* Update two buoys */
  @Test
  public void testMultipleBuoys() throws UnknownBuoyException {
    BuoyResponse buoy1Response =
        new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
    BuoyResponse buoy2Response =
        new BuoyResponse("temperature", 22.0, 2, System.currentTimeMillis());

    store.update(Arrays.asList(buoy1Response, buoy2Response));

    assertEquals(1, store.getHistory(1).size());
    assertEquals(1, store.getHistory(2).size());
    assertEquals(20.5, store.getHistory(1).get(0).measurementVal);
    assertEquals(22.0, store.getHistory(2).get(0).measurementVal);
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
    BuoyResponse response1 = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());

    store.update(Arrays.asList(response1, null));

    List<BuoyResponse> history = store.getHistory(1);
    assertEquals(1, history.size()); // null should be skipped
  }

  @Test
  public void testReturnedListIsIndependent() throws UnknownBuoyException {
    BuoyResponse response = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
    store.update(Arrays.asList(response));

    List<BuoyResponse> history1 = store.getHistory(1);
    List<BuoyResponse> history2 = store.getHistory(1);

    assertNotSame(history1, history2);
  }

  @Test
  public void testOldDataIsDiscarded() throws Exception {
    long now = System.currentTimeMillis();
    long eightDaysAgo = now - (8L * 24 * 60 * 60 * 1000);

    BuoyResponse old = new BuoyResponse("temperature", 10.0, 1, eightDaysAgo);
    BuoyResponse fresh = new BuoyResponse("temperature", 20.0, 1, now);

    store.update(Arrays.asList(old, fresh));

    List<BuoyResponse> history = store.getHistory(1);

    assertEquals(1, history.size());
    assertEquals(20.0, history.get(0).measurementVal);
  }

  @Test
  public void testGetLatestReturnsMostRecent() throws Exception {
    long oldTs = System.currentTimeMillis();
    long newTs = oldTs + 5000;

    BuoyResponse oldR = new BuoyResponse("temperature", 20.5, 1, oldTs);
    BuoyResponse newR = new BuoyResponse("temperature", 21.0, 1, newTs);

    store.update(Arrays.asList(oldR, newR));

    Optional<BuoyResponse> latest = store.getLatest(1, "temperature");
    assertTrue(latest.isPresent());
    assertEquals(21.0, latest.get().measurementVal);
  }

  @Test
  public void testGetLatestFiltersByType() throws Exception {
    BuoyResponse temp = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
    BuoyResponse salinity = new BuoyResponse("salinity", 35.0, 1, System.currentTimeMillis());

    store.update(Arrays.asList(temp, salinity));

    Optional<BuoyResponse> latest = store.getLatest(1, "salinity");

    assertTrue(latest.isPresent());
    assertEquals("salinity", latest.get().measurementType);
    assertEquals(35.0, latest.get().measurementVal);
  }

  @Test
  public void testGetLatestReturnsEmptyForMissingType() throws Exception {
    store.update(List.of(new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis())));

    Optional<BuoyResponse> latest = store.getLatest(1, "pressure");

    assertTrue(latest.isEmpty());
  }

  @Test
  public void testGetLatestUnknownBuoyThrowsException() {
    assertThrows(UnknownBuoyException.class, () -> store.getLatest(999, "temperature"));
  }

  @Test
  public void testSaveAndGetDeployment() {
    Deployment d = new Deployment(1, 42.0, -70.0, 30.0, System.currentTimeMillis());

    store.saveDeployment(d);

    Optional<Deployment> fetchedOpt = store.getDeployment(1);
    assertTrue(fetchedOpt.isPresent());

    Deployment fetched = fetchedOpt.get();
    assertEquals(42.0, fetched.lat);
    assertEquals(-70.0, fetched.lon);
    assertEquals(30.0, fetched.allowedRadiusMeters);
  }
}
