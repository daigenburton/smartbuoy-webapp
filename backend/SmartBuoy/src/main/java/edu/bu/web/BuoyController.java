package edu.bu.web;

import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import edu.bu.web.dto.HistoryEntry;
import edu.bu.web.dto.HistoryResponse;
import edu.bu.web.dto.MeasurementResponse;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** REST controller exposing buoy sensor data endpoints. */
@RestController
public class BuoyController {

  private final DataStore dataStore;

  /** Creates a BuoyController backed by the given DataStore. */
  public BuoyController(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  /** Returns history for a buoy, filtered to the last N hours if the hours param is provided. */
  @GetMapping("/history/{measurementType}/{buoyId}")
  public HistoryResponse getHistory(
      @PathVariable String measurementType,
      @PathVariable int buoyId,
      @RequestParam(required = false) Integer hours)
      throws UnknownBuoyException {

    List<BuoyResponse> history = dataStore.getHistory(buoyId);

    if (hours != null) {
      Instant cutoff = Instant.now().minusSeconds(hours * 3600L);
      history = history.stream().filter(r -> r.getTimestamp().isAfter(cutoff)).collect(Collectors.toList());
    }

    String type = measurementType.toLowerCase();
    List<HistoryEntry> entries = history.stream().map(r -> toHistoryEntry(r, buoyId, type)).collect(Collectors.toList());

    return new HistoryResponse(entries);
  }

  /** Returns the latest measurement for a specific type and buoy via /current path. */
  @GetMapping("/current/{measurementType}/{buoyId}")
  public MeasurementResponse getCurrent(
      @PathVariable String measurementType, @PathVariable int buoyId)
      throws UnknownBuoyException {

    BuoyResponse latest =
        dataStore
            .getLatest(buoyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No data for buoy " + buoyId));

    return toMeasurementResponse(latest, measurementType.toLowerCase());
  }

  /** Returns the latest temperature reading for a buoy. */
  @GetMapping("/temperature/{buoyId}")
  public MeasurementResponse getTemperature(@PathVariable int buoyId) throws UnknownBuoyException {
    BuoyResponse latest =
        dataStore
            .getLatest(buoyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No data for buoy " + buoyId));
    return MeasurementResponse.temperature(buoyId, latest.getTimestamp().toEpochMilli(), latest.getTemperature());
  }

  /** Returns the latest pressure reading for a buoy. */
  @GetMapping("/pressure/{buoyId}")
  public MeasurementResponse getPressure(@PathVariable int buoyId) throws UnknownBuoyException {
    BuoyResponse latest =
        dataStore
            .getLatest(buoyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No data for buoy " + buoyId));
    return MeasurementResponse.pressure(buoyId, latest.getTimestamp().toEpochMilli(), latest.getPressure());
  }

  /** Returns the latest location reading for a buoy. */
  @GetMapping("/location/{buoyId}")
  public MeasurementResponse getLocation(@PathVariable int buoyId) throws UnknownBuoyException {
    BuoyResponse latest =
        dataStore
            .getLatest(buoyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No data for buoy " + buoyId));
    return MeasurementResponse.location(buoyId, latest.getTimestamp().toEpochMilli(), latest.getLatitude(), latest.getLongitude());
  }

  private HistoryEntry toHistoryEntry(BuoyResponse response, int buoyId, String type) {
    long epochMillis = response.getTimestamp().toEpochMilli();
    switch (type) {
      case "temperature":
        return HistoryEntry.temperature(buoyId, epochMillis, response.getTemperature());
      case "pressure":
        return HistoryEntry.pressure(buoyId, epochMillis, response.getPressure());
      case "location":
        return HistoryEntry.location(buoyId, epochMillis, response.getLatitude(), response.getLongitude());
      default:
        throw new IllegalArgumentException("Invalid measurement type: " + type);
    }
  }

  private MeasurementResponse toMeasurementResponse(BuoyResponse response, String type) {
    long epochMillis = response.getTimestamp().toEpochMilli();
    switch (type) {
      case "temperature":
        return MeasurementResponse.temperature(response.getBuoyId(), epochMillis, response.getTemperature());
      case "pressure":
        return MeasurementResponse.pressure(response.getBuoyId(), epochMillis, response.getPressure());
      case "location":
        return MeasurementResponse.location(response.getBuoyId(), epochMillis, response.getLatitude(), response.getLongitude());
      default:
        throw new IllegalArgumentException("Invalid measurement type: " + type);
    }
  }
}
