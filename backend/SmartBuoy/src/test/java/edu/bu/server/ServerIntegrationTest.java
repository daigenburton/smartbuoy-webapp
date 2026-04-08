package edu.bu.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.bu.data.BuoyResponse;
import edu.bu.data.InMemoryStore;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for the SmartBuoy REST API endpoints. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("IntegrationTest")
public class ServerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private InMemoryStore store;

  @BeforeEach
  public void setUp() {
    store.clearAll();
    store.update(Arrays.asList(new BuoyResponse(1, Instant.now(), 22.5, 101325.0, 42.36, -71.05)));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testHistoryEndpointReturnsData() throws Exception {
    mockMvc
        .perform(get("/history/temperature/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.history[0].buoyId").value(1))
        .andExpect(jsonPath("$.history[0].measurementType").value("temperature"))
        .andExpect(jsonPath("$.history[0].temperature").value(22.5));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testUnknownBuoyReturns404() throws Exception {
    mockMvc.perform(get("/history/temperature/999")).andExpect(status().isNotFound());
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testContentTypeIsJson() throws Exception {
    mockMvc
        .perform(get("/history/temperature/1"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testHistoryEndpointMultipleReadings() throws Exception {
    store.update(
        Arrays.asList(
            new BuoyResponse(1, Instant.now(), 22.5, 101325.0, 42.36, -71.05),
            new BuoyResponse(1, Instant.now().plusSeconds(60), 23.0, 101320.0, 42.37, -71.06)));

    String body =
        mockMvc
            .perform(get("/history/temperature/1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertTrue(body.contains("22.5"));
    assertTrue(body.contains("23.0"));
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  public void testServerHandlesRapidRequests() throws Exception {
    int threadCount = 50;
    int requestsPerThread = 20;
    Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      threads[i] =
          new Thread(
              () -> {
                for (int j = 0; j < requestsPerThread; j++) {
                  try {
                    mockMvc
                        .perform(get("/history/temperature/1"))
                        .andExpect(status().isOk());
                  } catch (Exception e) {
                    fail("Server failed under load: " + e.getMessage());
                  }
                }
              });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testLatestTemperatureEndpoint() throws Exception {
    mockMvc
        .perform(get("/temperature/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.measurementType").value("temperature"))
        .andExpect(jsonPath("$.temperature").value(22.5));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testLatestPressureEndpoint() throws Exception {
    mockMvc
        .perform(get("/pressure/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.measurementType").value("pressure"))
        .andExpect(jsonPath("$.pressure").value(101325.0));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testLatestLocationEndpoint() throws Exception {
    mockMvc
        .perform(get("/location/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latitude").value(42.36))
        .andExpect(jsonPath("$.longitude").value(-71.05));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testLatestEndpointInvalidBuoyIdReturns404() throws Exception {
    mockMvc.perform(get("/temperature/999")).andExpect(status().isNotFound());
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testCurrentTemperatureEndpoint() throws Exception {
    mockMvc
        .perform(get("/current/temperature/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.measurementType").value("temperature"))
        .andExpect(jsonPath("$.temperature").value(22.5));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testCurrentPressureEndpoint() throws Exception {
    mockMvc
        .perform(get("/current/pressure/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.measurementType").value("pressure"))
        .andExpect(jsonPath("$.pressure").value(101325.0));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testCurrentLocationEndpoint() throws Exception {
    mockMvc
        .perform(get("/current/location/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latitude").value(42.36))
        .andExpect(jsonPath("$.longitude").value(-71.05));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testCurrentInvalidBuoyReturns404() throws Exception {
    mockMvc.perform(get("/current/temperature/999")).andExpect(status().isNotFound());
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testHistoryWithMeasurementType() throws Exception {
    mockMvc
        .perform(get("/history/temperature/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.history[0].measurementType").value("temperature"))
        .andExpect(jsonPath("$.history[0].temperature").value(22.5));
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testHistoryHoursFilter() throws Exception {
    store.update(
        Arrays.asList(
            new BuoyResponse(1, Instant.now().minusSeconds(7200), 20.0, 101000.0, 42.0, -71.0),
            new BuoyResponse(1, Instant.now(), 25.0, 101200.0, 42.0, -71.0)));

    String body =
        mockMvc
            .perform(get("/history/temperature/1?hours=1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertTrue(body.contains("25.0"));
    assertFalse(body.contains("20.0"));
  }
}
