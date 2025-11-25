package edu.bu.server;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.data.BuoyResponse;
import edu.bu.data.InMemoryStore;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Tag("IntegrationTest")
public class ServerIntegrationTest {

  private InMemoryStore store;
  private BasicWebServer server;

  @BeforeEach
  public void setUp() throws Exception {
    store = new InMemoryStore();
    store.update(
        Arrays.asList(new BuoyResponse("temperature", 22.5, 1, System.currentTimeMillis())));

    server = new BasicWebServer(store);
    new Thread(
            () -> {
              try {
                server.start();
              } catch (Exception e) {
                e.printStackTrace();
              }
            })
        .start();

    Thread.sleep(500);
  }

  /* Verify endpoint retuns a 200 OK response with expected buoy data */
  @Test
  @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
  public void testHistoryEndpointReturnsData() throws Exception {
    URL url = new URL("http://localhost:8000/history/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(200, connection.getResponseCode());

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      response.append(line);
    }
    reader.close();

    String json = response.toString();
    assertTrue(json.contains("\"buoyId\":1"));
    assertTrue(json.contains("\"measurementType\":\"temperature\""));
  }

  /* Verify API handles requests for a nonexistent buoy*/
  @Test
  @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
  public void testUnknownBuoyReturns404() throws Exception {
    URL url = new URL("http://localhost:8000/history/999");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(404, connection.getResponseCode());
  }

  /* Verify valid JSON is returned*/
  @Test
  @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
  public void testContentTypeIsJson() throws Exception {
    URL url = new URL("http://localhost:8000/history/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals("application/json", connection.getHeaderField("Content-Type"));
  }

  /* Verify server returns entire history of a buoy */
  @Test
  @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
  public void testHistoryEndpointMultipleReadings() throws Exception {
    store.update(
        Arrays.asList(
            new BuoyResponse("temperature", 22.5, 1, System.currentTimeMillis()),
            new BuoyResponse("salinity", 35.0, 1, System.currentTimeMillis())));

    URL url = new URL("http://localhost:8000/history/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(200, connection.getResponseCode());

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      response.append(line);
    }
    reader.close();

    String json = response.toString();
    assertTrue(json.contains("\"measurementType\":\"temperature\""));
    assertTrue(json.contains("\"measurementType\":\"salinity\""));
  }

  /* Verify server can handle being hammered with many requests */
  @Test
  @Timeout(value = 2, unit = TimeUnit.SECONDS)
  public void testServerHandlesRapidRequests() throws Exception {
    int threadCount = 50; // number of parallel clients hitting API
    int requestsPerThread = 20; // total = 1000 requests
    Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      threads[i] =
          new Thread(
              () -> {
                for (int j = 0; j < requestsPerThread; j++) {
                  try {
                    URL url = new URL("http://localhost:8000/history/1");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int code = conn.getResponseCode();
                    assertEquals(200, code);
                    conn.disconnect();
                  } catch (Exception e) {
                    fail("Server failed under load: " + e.getMessage());
                  }
                }
              });
      threads[i].start();
    }

    for (Thread t : threads) {
      t.join();
    }
  }

  /// new tests-

  @Test
  @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
  public void testLatestTemperatureEndpoint() throws Exception {
    URL url = new URL("http://localhost:8000/temperature/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(200, connection.getResponseCode());

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String body = reader.lines().reduce("", String::concat);

    assertTrue(body.contains("\"measurementType\":\"temperature\""));
    assertTrue(body.contains("\"measurementVal\":"));
  }

  @Test
  @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
  public void testLatestEndpointMissingDataReturns404() throws Exception {
    URL url = new URL("http://localhost:8000/pressure/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(404, connection.getResponseCode());
  }

  @Test
  @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
  public void testLatestEndpointInvalidBuoyIdReturns404() throws Exception {
    URL url = new URL("http://localhost:8000/temperature/999");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(404, connection.getResponseCode());
  }
}
