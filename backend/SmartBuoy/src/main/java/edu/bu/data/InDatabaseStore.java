package edu.bu.data;

import edu.bu.analytics.UnknownBuoyException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.tinylog.Logger;

/** MySQL-backed data store for buoy readings */
public class InDatabaseStore implements DataStore {

  private final String jdbcUrl;
  private final String username;
  private final String password;

  public InDatabaseStore(String jdbcUrl, String username, String password) {
    this.jdbcUrl = jdbcUrl;
    this.username = username;
    this.password = password;
    initializeDatabase();
  }

  /** Initialize database connection and verify table exists */
  private void initializeDatabase() {
    try (Connection conn = getConnection()) {
      Logger.info("Successfully connected to database");
    } catch (SQLException e) {
      Logger.error("Failed to connect to database: {}", e.getMessage());
      throw new RuntimeException("Database initialization failed", e);
    }
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  @Override
  public void update(List<BuoyResponse> responses) {
    String insertSql =
        "INSERT INTO buoy_data (buoy_id, measurement_type, measurement_value, timestamp_ms) "
            + "VALUES (?, ?, ?, ?)";

    long oneWeekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;

    try (Connection conn = getConnection();
        PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

      // Insert new responses
      for (BuoyResponse response : responses) {
        if (response == null) continue;

        insertStmt.setInt(1, response.buoyId);
        insertStmt.setString(2, response.measurementType);
        insertStmt.setDouble(3, response.measurementVal);
        insertStmt.setLong(4, response.msSinceEpoch);
        insertStmt.addBatch();
      }

      insertStmt.executeBatch();

      // Clean up old data (older than one week)
      String deleteSql = "DELETE FROM buoy_data WHERE timestamp_ms < ?";
      try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
        deleteStmt.setLong(1, oneWeekAgo);
        int deleted = deleteStmt.executeUpdate();
        if (deleted > 0) {
          Logger.info("Deleted {} old records", deleted);
        }
      }

      Logger.info("Successfully stored {} buoy responses", responses.size());

    } catch (SQLException e) {
      Logger.error("Failed to update database: {}", e.getMessage());
      throw new RuntimeException("Database update failed", e);
    }
  }

  @Override
  public List<BuoyResponse> getHistory(int buoyId) throws UnknownBuoyException {
    String sql =
        "SELECT buoy_id, measurement_type, measurement_value, timestamp_ms "
            + "FROM buoy_data "
            + "WHERE buoy_id = ? "
            + "ORDER BY timestamp_ms ASC";

    List<BuoyResponse> history = new ArrayList<>();

    try (Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, buoyId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        BuoyResponse response =
            new BuoyResponse(
                rs.getString("measurement_type"),
                rs.getDouble("measurement_value"),
                rs.getInt("buoy_id"),
                rs.getLong("timestamp_ms"));
        history.add(response);
      }

      if (history.isEmpty()) {
        throw new UnknownBuoyException(buoyId);
      }

      return history;

    } catch (SQLException e) {
      Logger.error("Failed to retrieve history for buoy {}: {}", buoyId, e.getMessage());
      throw new RuntimeException("Database query failed", e);
    }
  }

  @Override
  public Optional<BuoyResponse> getLatest(int buoyId, String measurementType)
      throws UnknownBuoyException {

    // First check if buoy exists
    String checkSql = "SELECT COUNT(*) FROM buoy_data WHERE buoy_id = ?";

    try (Connection conn = getConnection();
        PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

      checkStmt.setInt(1, buoyId);
      ResultSet checkRs = checkStmt.executeQuery();

      if (checkRs.next() && checkRs.getInt(1) == 0) {
        throw new UnknownBuoyException(buoyId);
      }
    } catch (SQLException e) {
      Logger.error("Failed to check buoy existence: {}", e.getMessage());
      throw new RuntimeException("Database query failed", e);
    }

    // Get latest measurement
    String sql =
        "SELECT buoy_id, measurement_type, measurement_value, timestamp_ms "
            + "FROM buoy_data "
            + "WHERE buoy_id = ? AND measurement_type = ? "
            + "ORDER BY timestamp_ms DESC "
            + "LIMIT 1";

    try (Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, buoyId);
      stmt.setString(2, measurementType);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        BuoyResponse response =
            new BuoyResponse(
                rs.getString("measurement_type"),
                rs.getDouble("measurement_value"),
                rs.getInt("buoy_id"),
                rs.getLong("timestamp_ms"));

        return Optional.of(response);
      }
      return Optional.empty();

    } catch (SQLException e) {
      Logger.error(
          "Failed to retrieve latest {} for buoy {}: {}", measurementType, buoyId, e.getMessage());
      throw new RuntimeException("Database query failed", e);
    }
  }
}
