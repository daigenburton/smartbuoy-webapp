package edu.bu.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.tinylog.Logger;

/** Manages database configuration from properties file */
public class DatabaseConfig {

  private final String jdbcUrl;
  private final String username;
  private final String password;

  public DatabaseConfig(String propertiesFile) {
    Properties props = new Properties();

    try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {

      if (input == null) {
        throw new RuntimeException("Unable to find " + propertiesFile);
      }

      props.load(input);

      this.jdbcUrl = props.getProperty("db.url");
      this.username = props.getProperty("db.username");
      this.password = props.getProperty("db.password");

      if (jdbcUrl == null || username == null || password == null) {
        throw new RuntimeException("Missing required database properties");
      }

      Logger.info("Database configuration loaded successfully");

    } catch (IOException e) {
      throw new RuntimeException("Failed to load database configuration", e);
    }
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
