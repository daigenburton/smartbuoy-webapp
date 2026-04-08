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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** In-memory data store used to hold buoy readings. */
@Service
@ConditionalOnProperty(name = "influxdb.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryStore implements DataStore {

  private final Map<Integer, List<BuoyResponse>> storedData = new ConcurrentHashMap<>();
  private final Map<Integer, Deployment> deployments = new ConcurrentHashMap<>();

  @Override
  public void update(List<BuoyResponse> responses) {
    Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
    for (BuoyResponse response : responses) {
      if (response == null) continue;
      int buoyId = response.getBuoyId();
      List<BuoyResponse> list =
          storedData.computeIfAbsent(buoyId, ignored -> Collections.synchronizedList(new ArrayList<>()));
      list.add(response);
      list.removeIf(r -> r.getTimestamp().isBefore(oneWeekAgo));
    }
  }

  @Override
  public List<BuoyResponse> getHistory(int buoyId) throws UnknownBuoyException {
    if (!storedData.containsKey(buoyId)) {
      throw new UnknownBuoyException(buoyId);
    }
    return new ArrayList<>(storedData.get(buoyId));
  }

  @Override
  public Optional<BuoyResponse> getLatest(int buoyId) throws UnknownBuoyException {
    if (!storedData.containsKey(buoyId)) {
      throw new UnknownBuoyException(buoyId);
    }
    return storedData.get(buoyId).stream().max(Comparator.comparing(BuoyResponse::getTimestamp));
  }

  @Override
  public void saveDeployment(Deployment deployment) {
    deployments.put(deployment.buoyId, deployment);
  }

  @Override
  public Optional<Deployment> getDeployment(int buoyId) {
    return Optional.ofNullable(deployments.get(buoyId));
  }

  /** Clears all stored data. Used in tests to reset state between runs. */
  public void clearAll() {
    storedData.clear();
    deployments.clear();
  }
}
