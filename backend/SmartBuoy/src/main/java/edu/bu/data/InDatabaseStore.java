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

  private void initializeDatabase() {
    try (Connection unused = getConnection()) {
      Logger.info("Successfully connected to database");
    } catch (SQLException e) {
      Logger.error("Failed to connect to database: {}", e.getMessage());
      throw new RuntimeException("Database initialization failed", e);
    }
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  private void insertResponses(Connection connection, List<BuoyResponse> responses)
      throws SQLException {

    String insertSql =
        "INSERT INTO buoy_data (buoyId, measurementType, measurementVal, timestamp) "
            + "VALUES (?, ?, ?, ?)";

    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
      for (BuoyResponse response : responses) {
        if (response == null) continue;

        insertStmt.setInt(1, response.buoyId);
        insertStmt.setString(2, response.measurementType);
        insertStmt.setDouble(3, response.measurementVal);
        insertStmt.setLong(4, response.timestamp);
        insertStmt.addBatch();
      }
      insertStmt.executeBatch();
    }
  }

  private void deleteOldData(Connection connection, long cutoffTimestamp) throws SQLException {

    String deleteSql = "DELETE FROM buoy_data WHERE timestamp < ?";

    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
      deleteStmt.setLong(1, cutoffTimestamp);
      int deleted = deleteStmt.executeUpdate();
      if (deleted > 0) {
        Logger.info("Deleted {} old data", deleted);
      }
    }
  }

  private void checkBuoyExists(int buoyId) throws UnknownBuoyException {
    String checkSql = "SELECT COUNT(*) FROM buoy_data WHERE buoyId = ?";

    try (Connection connection = getConnection();
        PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {

      checkStmt.setInt(1, buoyId);
      try (ResultSet checkRs = checkStmt.executeQuery()) {
        if (checkRs.next() && checkRs.getInt(1) == 0) {
          throw new UnknownBuoyException(buoyId);
        }

        if (checkRs.next() && checkRs.getInt(1) == 0) {
          throw new UnknownBuoyException(buoyId);
        }
      }
    } catch (SQLException e) {
      Logger.error("Failed to check buoy existence: {}", e.getMessage());
      throw new RuntimeException("Database query failed", e);
    }
  }

  @Override
  public void update(List<BuoyResponse> responses) {
    long oneWeekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;

    try (Connection connection = getConnection()) {
      insertResponses(connection, responses);
      deleteOldData(connection, oneWeekAgo);
      Logger.info("Successfully stored {} buoy responses", responses.size());
    } catch (SQLException e) {
      Logger.error("Failed to update database: {}", e.getMessage());
      throw new RuntimeException("Database update failed", e);
    }
  }

  @Override
  public List<BuoyResponse> getHistory(int buoyId) throws UnknownBuoyException {
    String sql =
        "SELECT buoyId, measurementType, measurementVal, timestamp "
            + "FROM buoy_data "
            + "WHERE buoyId = ? "
            + "ORDER BY timestamp ASC";

    List<BuoyResponse> history = new ArrayList<>();

    try (Connection connection = getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      stmt.setInt(1, buoyId);
      try (ResultSet result = stmt.executeQuery()) {

        while (result.next()) {
          BuoyResponse response =
              new BuoyResponse(
                  result.getString("measurementType"),
                  result.getDouble("measurementVal"),
                  result.getInt("buoyId"),
                  result.getLong("timestamp"));
          history.add(response);
        }
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
    checkBuoyExists(buoyId);

    // Get latest measurement
    String sql =
        "SELECT buoyId, measurementType, measurementVal, timestamp "
            + "FROM buoy_data "
            + "WHERE buoyId = ? AND measurementType = ? "
            + "ORDER BY timestamp DESC "
            + "LIMIT 1";

    try (Connection connection = getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      stmt.setInt(1, buoyId);
      stmt.setString(2, measurementType);

      try (ResultSet result = stmt.executeQuery()) {

        if (result.next()) {
          BuoyResponse response =
              new BuoyResponse(
                  result.getString("measurementType"),
                  result.getDouble("measurementVal"),
                  result.getInt("buoyId"),
                  result.getLong("timestamp"));

          return Optional.of(response);
        }

        return Optional.empty();
      }

    } catch (SQLException e) {
      Logger.error(
          "Failed to retrieve latest {} for buoy {}: {}", measurementType, buoyId, e.getMessage());
      throw new RuntimeException("Database query failed", e);
    }
  }
}
