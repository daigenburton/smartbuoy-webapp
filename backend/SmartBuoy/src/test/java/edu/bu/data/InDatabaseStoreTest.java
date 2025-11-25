package edu.bu.data;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.analytics.UnknownBuoyException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;

@Tag("DBTest")
/** Test suite for InDatabaseStore */
class InDatabaseStoreTest {

  private static final String TEST_DB_URL =
      "jdbc:mysql://localhost:3306/smartbuoy_test?useSSL=false&serverTimezone=UTC";
  private static final String TEST_DB_USER = "user";
  private static final String TEST_DB_PASSWORD = "buoy";

  private InDatabaseStore store;

  @BeforeAll
  static void setupDatabase() throws SQLException {
    // Create test database if it doesn't exist
    try (Connection conn =
        DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC",
            TEST_DB_USER,
            TEST_DB_PASSWORD)) {
      Statement stmt = conn.createStatement();
      stmt.execute("CREATE DATABASE IF NOT EXISTS smartbuoy_test");
      stmt.execute("USE smartbuoy_test");
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS buoy_data ("
              + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
              + "buoyId INT NOT NULL, "
              + "measurementType VARCHAR(50) NOT NULL, "
              + "measurementVal DOUBLE NOT NULL, "
              + "timestamp BIGINT NOT NULL, "
              + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
              + "INDEX idx_buoy_timestamp (buoyId, timestamp), "
              + "INDEX idx_buoy_type (buoyId, measurementType, timestamp))");
    }
  }

  @BeforeEach
  void setUp() throws SQLException {
    // Create a fresh store for each test
    store = new InDatabaseStore(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);

    // Clear all data before each test
    clearDatabase();
  }

  @AfterEach
  void tearDown() throws SQLException {
    clearDatabase();
  }

  private void clearDatabase() throws SQLException {
    try (Connection conn =
            DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM buoy_data");
    }
  }

  @Test
  void testUpdateStoresData() throws InterruptedException {
    List<BuoyResponse> responses = new ArrayList<>();
    responses.add(new BuoyResponse("temperature", 22.5, 1, System.currentTimeMillis()));
    responses.add(new BuoyResponse("pressure", 1013.2, 1, System.currentTimeMillis()));

    assertDoesNotThrow(() -> store.update(responses));

    store.update(responses);

    // for checking in mysql durring execution-
    // System.out.println("Sleeping... Check smartbuoy_test.buoy_data NOW!");
    // Thread.sleep(10000);
  }

  @Test
  void testGetHistoryReturnsAllData() throws UnknownBuoyException {
    long now = System.currentTimeMillis();
    List<BuoyResponse> responses = new ArrayList<>();
    responses.add(new BuoyResponse("temperature", 22.5, 1, now));
    responses.add(new BuoyResponse("pressure", 1013.2, 1, now + 1000));
    responses.add(new BuoyResponse("temperature", 23.0, 1, now + 2000));

    store.update(responses);

    List<BuoyResponse> history = store.getHistory(1);

    assertEquals(3, history.size());
    assertEquals(22.5, history.get(0).measurementVal);
    assertEquals(1013.2, history.get(1).measurementVal);
    assertEquals(23.0, history.get(2).measurementVal);
  }

  @Test
  void testGetHistoryThrowsForUnknownBuoy() {
    assertThrows(UnknownBuoyException.class, () -> store.getHistory(999));
  }

  @Test
  void testGetLatestReturnsNewestMeasurement() throws UnknownBuoyException {
    long now = System.currentTimeMillis();
    List<BuoyResponse> responses = new ArrayList<>();
    responses.add(new BuoyResponse("temperature", 20.0, 1, now - 2000));
    responses.add(new BuoyResponse("temperature", 22.5, 1, now - 1000));
    responses.add(new BuoyResponse("temperature", 25.0, 1, now));
    responses.add(new BuoyResponse("pressure", 1013.2, 1, now));

    store.update(responses);

    Optional<BuoyResponse> latest = store.getLatest(1, "temperature");

    assertTrue(latest.isPresent());
    assertEquals(25.0, latest.get().measurementVal);
    assertEquals(now, latest.get().timestamp);
  }

  @Test
  void testGetLatestReturnsEmptyForMissingMeasurementType() throws UnknownBuoyException {
    List<BuoyResponse> responses = new ArrayList<>();
    responses.add(new BuoyResponse("temperature", 22.5, 1, System.currentTimeMillis()));

    store.update(responses);

    Optional<BuoyResponse> latest = store.getLatest(1, "pressure");

    assertFalse(latest.isPresent());
  }

  @Test
  void testGetLatestThrowsForUnknownBuoy() {
    assertThrows(UnknownBuoyException.class, () -> store.getLatest(999, "temperature"));
  }

  @Test
  void testMultipleBuoys() throws UnknownBuoyException {
    long now = System.currentTimeMillis();
    List<BuoyResponse> responses = new ArrayList<>();
    responses.add(new BuoyResponse("temperature", 22.5, 1, now));
    responses.add(new BuoyResponse("temperature", 18.0, 2, now));
    responses.add(new BuoyResponse("pressure", 1013.2, 1, now));

    store.update(responses);

    List<BuoyResponse> history1 = store.getHistory(1);
    List<BuoyResponse> history2 = store.getHistory(2);

    assertEquals(2, history1.size());
    assertEquals(1, history2.size());
    assertEquals(18.0, history2.get(0).measurementVal);
  }

  @Test
  void testOldDataCleanup() throws UnknownBuoyException, InterruptedException {
    long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
    long twoWeeksAgo = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000);
    long now = System.currentTimeMillis();

    List<BuoyResponse> oldData = new ArrayList<>();
    oldData.add(new BuoyResponse("temperature", 10.0, 1, twoWeeksAgo));

    List<BuoyResponse> recentData = new ArrayList<>();
    recentData.add(new BuoyResponse("temperature", 20.0, 1, now));

    store.update(oldData);
    store.update(recentData);

    List<BuoyResponse> history = store.getHistory(1);

    // Should only have recent data, old data should be cleaned up
    assertEquals(1, history.size());
    assertEquals(20.0, history.get(0).measurementVal);
  }

  @Test
  void testUpdateWithNullResponse() {
    List<BuoyResponse> responses = new ArrayList<>();
    responses.add(new BuoyResponse("temperature", 22.5, 1, System.currentTimeMillis()));
    responses.add(null);
    responses.add(new BuoyResponse("pressure", 1013.2, 1, System.currentTimeMillis()));

    assertDoesNotThrow(() -> store.update(responses));
  }

  @Test
  void testHistoryOrderedByTimestamp() throws UnknownBuoyException {
    long now = System.currentTimeMillis();
    List<BuoyResponse> responses = new ArrayList<>();
    responses.add(new BuoyResponse("temperature", 25.0, 1, now + 2000));
    responses.add(new BuoyResponse("temperature", 20.0, 1, now));
    responses.add(new BuoyResponse("temperature", 22.5, 1, now + 1000));

    store.update(responses);

    List<BuoyResponse> history = store.getHistory(1);

    assertEquals(3, history.size());
    assertTrue(history.get(0).timestamp <= history.get(1).timestamp);
    assertTrue(history.get(1).timestamp <= history.get(2).timestamp);
  }
}
