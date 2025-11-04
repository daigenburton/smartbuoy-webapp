package edu.bu.data;

import java.time.Instant;

public class BuoyResponse {
  public final String symbol;
  public final double price;
  public final long msSinceEpoch;
  public final long volume;

  public BuoyResponse(String symbol, double price, long msSinceEpoch, long volume) {
    this.symbol = symbol;
    this.price = price;
    this.msSinceEpoch = msSinceEpoch;
    this.volume = volume;
  }

  @Override
  public String toString() {
    return "FinhubResponse{"
        + "symbol='"
        + symbol
        + '\''
        + ", price="
        + price
        + ", msSinceEpoch="
        + Instant.ofEpochMilli(msSinceEpoch)
        + ", volume="
        + volume
        + '}';
  }
}
