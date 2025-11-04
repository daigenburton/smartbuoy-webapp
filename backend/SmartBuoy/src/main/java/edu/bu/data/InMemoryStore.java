package edu.bu.data;

import java.util.List;

public class InMemoryStore implements DataStore {

  @Override
  public void update(List<BuoyResponse> responses) {}

  @Override
  public List<BuoyResponse> getHistory(int buoy) {
    return List.of();
  }
}
