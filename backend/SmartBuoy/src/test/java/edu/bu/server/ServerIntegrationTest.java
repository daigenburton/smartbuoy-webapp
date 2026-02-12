package edu.bu.server;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.data.BuoyResponse;
import edu.bu.data.InMemoryStore;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
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
    store.update(Arrays.asList(new BuoyResponse(1, Instant.now(), 22.5, 101325.0, 42.36, -71.05)));

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

  /* Verify endpoint returns a 200 OK response with expected buoy data */
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
    assertTrue(json.contains("\"temperature\":22.5"));
    assertTrue(json.contains("\"pressure\":101325.0"));
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
            new BuoyResponse(1, Instant.now(), 22.5, 101325.0, 42.36, -71.05),
            new BuoyResponse(1, Instant.now().plusSeconds(60), 23.0, 101320.0, 42.37, -71.06)));

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
    assertTrue(json.contains("\"temperature\":22.5"));
    assertTrue(json.contains("\"temperature\":23.0"));
    assertTrue(json.contains("\"pressure\":101325.0"));
    assertTrue(json.contains("\"pressure\":101320.0"));
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
    assertTrue(body.contains("\"measurementVal\":22.5"));
  }

  @Test
  @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
  public void testLatestPressureEndpoint() throws Exception {
    URL url = new URL("http://localhost:8000/pressure/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(200, connection.getResponseCode());

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String body = reader.lines().reduce("", String::concat);

    assertTrue(body.contains("\"measurementType\":\"pressure\""));
    assertTrue(body.contains("\"measurementVal\":101325.0"));
  }

  @Test
  @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
  public void testLatestLocationEndpoint() throws Exception {
    URL url = new URL("http://localhost:8000/location/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(200, connection.getResponseCode());

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String body = reader.lines().reduce("", String::concat);

    assertTrue(body.contains("\"latitude\":42.36"));
    assertTrue(body.contains("\"longitude\":-71.05"));
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
