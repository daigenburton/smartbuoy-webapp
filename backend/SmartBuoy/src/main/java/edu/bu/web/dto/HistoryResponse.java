package edu.bu.web.dto;

import java.util.List;

/** Wraps a list of HistoryEntry records for the /history endpoint response. */
public class HistoryResponse {

  private final List<HistoryEntry> history;

  /** Creates a HistoryResponse with the given list of entries. */
  public HistoryResponse(List<HistoryEntry> history) {
    this.history = history;
  }

  public List<HistoryEntry> getHistory() {
    return history;
  }
}
