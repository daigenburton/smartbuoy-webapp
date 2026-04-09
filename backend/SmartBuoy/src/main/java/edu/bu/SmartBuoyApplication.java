package edu.bu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import edu.bu.shadow.ShadowService;
import edu.bu.shadow.ShadowUpdateMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;

import java.time.Instant;
import java.util.List;

/** Entry point for the SmartBuoy Spring Boot application. */
@SpringBootApplication
public class SmartBuoyApplication {

  /** Main method that starts the Spring Boot application. */
  public static void main(String[] args) {
    SpringApplication.run(SmartBuoyApplication.class, args);
  }
}
