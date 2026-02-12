package edu.bu.data;

import edu.bu.analytics.UnknownBuoyException;
import java.util.*;

/**
 * Implementors store data that is received from the buoy and make it available for internal
 * computations
 */
public interface DataStore {
  /** Handle an update received from the buoy. */
  void update(List<BuoyResponse> responses);

  /** Get the entire history of updates that we have seen for buoy. */
  List<BuoyResponse> getHistory(int buoy) throws UnknownBuoyException;

  Optional<BuoyResponse> getLatest(int buoyId) throws UnknownBuoyException;
}
