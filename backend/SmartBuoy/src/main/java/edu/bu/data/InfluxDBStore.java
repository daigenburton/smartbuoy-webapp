package edu.bu.data;

import edu.bu.analytics.UnknownBuoyException;
import java.util.List;
import java.util.Optional;

/** Persistent data store used to hold buoy readings with InfluxDB */
public class InfluxDBStore implements DataStore {

  @Override
  public void update(List<BuoyResponse> responses) {}

  @Override
  public List<BuoyResponse> getHistory(int buoy) throws UnknownBuoyException {
    return List.of();
  }

  @Override
  public Optional<BuoyResponse> getLatest(int buoyId) throws UnknownBuoyException {
    return Optional.empty();
  }
}
