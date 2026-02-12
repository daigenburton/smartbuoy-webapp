package edu.bu.data;

import edu.bu.analytics.UnknownBuoyException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory data store used to hold buoy readings */
public class InMemoryStore implements DataStore {

  /* Maps buoyId to a list of that buoy's data */
  private final Map<Integer, List<BuoyResponse>> storedData = new ConcurrentHashMap<>();

  /* Update data stored in memory for buoy */
  @Override
  public void update(List<BuoyResponse> responses) {

    Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

    // loop through all new sensor readings, get buoyId, add new readings to buoy's list
    for (BuoyResponse response : responses) {
      if (response == null) continue;
      int buoyId = response.getBuoyId();

      List<BuoyResponse> list =
          storedData.computeIfAbsent(buoyId, k -> Collections.synchronizedList(new ArrayList<>()));

      list.add(response);

      // remove values older than a week old
      list.removeIf(r -> r.getTimestamp().isBefore(oneWeekAgo));
    }
  }

  /* If the buoy exists, retrieve all of its the stored data */
  @Override
  public List<BuoyResponse> getHistory(int buoyId) throws UnknownBuoyException {

    if (!storedData.containsKey(buoyId)) {
      throw new UnknownBuoyException(buoyId);
    }

    List<BuoyResponse> history = storedData.get(buoyId);
    return new ArrayList<>(history);
  }

  /* Get the most recent reading for a buoy */
  @Override
  public Optional<BuoyResponse> getLatest(int buoyId) throws UnknownBuoyException {

    if (!storedData.containsKey(buoyId)) {
      throw new UnknownBuoyException(buoyId);
    }

    return storedData.get(buoyId).stream().max(Comparator.comparing(BuoyResponse::getTimestamp));
  }
}
