package edu.bu.server;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.data.BuoyResponse;
import edu.bu.data.InMemoryStore;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  public void testUnknownBuoyReturns404() throws Exception {
    URL url = new URL("http://localhost:8000/history/999");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals(404, connection.getResponseCode());
  }

  /* Verify valid JSON is returned*/
  @Test
  public void testContentTypeIsJson() throws Exception {
    URL url = new URL("http://localhost:8000/history/1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    assertEquals("application/json", connection.getHeaderField("Content-Type"));
  }

  /* Verify server returns entire history of a buoy */
  @Test
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
}
