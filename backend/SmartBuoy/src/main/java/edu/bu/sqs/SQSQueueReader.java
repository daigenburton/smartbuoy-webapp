package edu.bu.sqs;

import edu.bu.analytics.geofence.GeofenceService;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import edu.bu.data.Deployment;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Continuously polls AWS SQS for new buoy messages and updates the DataStore. Uses exponential
 * back-off when the queue is empty.
 */
@Component
public class SQSQueueReader {

  private static final Logger log = LoggerFactory.getLogger(SQSQueueReader.class);
  private static final long BASE_SLEEP_MS = 100;
  private static final long INITIAL_BACKOFF_MS = 500;
  private static final long MAX_BACKOFF_MS = 60000;
  private static final int BACKOFF_MULTIPLIER = 2;
  private static final int SQS_WAIT_TIME_SECONDS = 10;

  private final DataStore dataStore;
  private final SqsClient sqsClient;
  private final String sqsQueueName;
  private String queueUrl;
  private long currentBackoffMs;
  private volatile boolean running = true;
  private Thread pollingThread;

  /** Constructs a SQSQueueReader with the provided DataStore and configuration. */
  public SQSQueueReader(
      DataStore dataStore,
      @Value("${sqs.queue-name:smartbuoy}") String sqsQueueName,
      @Value("${AWS_REGION:us-east-1}") String awsRegion) {
    this.dataStore = dataStore;
    this.sqsQueueName = sqsQueueName;
    this.sqsClient = SqsClient.builder().region(Region.of(awsRegion)).build();
    this.currentBackoffMs = INITIAL_BACKOFF_MS;
  }

  /** Starts the SQS polling thread after the Spring context is fully initialized. */
  @PostConstruct
  public void start() {
    this.queueUrl = getQueueUrl(sqsClient, sqsQueueName);
    pollingThread = new Thread(this::runPollingLoop, "sqs-reader");
    pollingThread.setDaemon(true);
    pollingThread.start();
    log.info("SQSQueueReader started, polling queue: {}", sqsQueueName);
  }

  /** Shuts down the SQS client when the Spring context closes. */
  @PreDestroy
  public void shutdown() {
    running = false;
    if (pollingThread != null) {
      pollingThread.interrupt();
    }
    if (sqsClient != null) {
      sqsClient.close();
      log.info("SQSQueueReader shutdown complete");
    }
  }

  private void runPollingLoop() {
    while (running) {
      try {
        pollQueue();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        log.error("Error in SQSQueueReader: {}", e.getMessage(), e);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
  }

  private void pollQueue() throws Exception {
    ReceiveMessageRequest receiveRequest =
        ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(SQS_WAIT_TIME_SECONDS)
            .build();

    ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
    List<Message> messages = response.messages();

    if (!messages.isEmpty()) {
      log.info("Received {} message(s) from SQS", messages.size());
      for (Message message : messages) {
        try {
          processMessage(message.body());
          deleteMessage(message.receiptHandle());
        } catch (Exception e) {
          log.error("Error processing message {}: {}", message.messageId(), e.getMessage());
        }
      }
      currentBackoffMs = INITIAL_BACKOFF_MS;
      Thread.sleep(BASE_SLEEP_MS);
    } else {
      log.debug("Queue empty, backing off for {}ms", currentBackoffMs);
      Thread.sleep(currentBackoffMs);
      currentBackoffMs = Math.min(currentBackoffMs * BACKOFF_MULTIPLIER, MAX_BACKOFF_MS);
    }
  }

  private void deleteMessage(String receiptHandle) {
    try {
      sqsClient.deleteMessage(
          DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(receiptHandle).build());
    } catch (SqsException e) {
      log.error("Failed to delete message: {}", e.awsErrorDetails().errorMessage());
    }
  }

  private static String getQueueUrl(SqsClient client, String queueName) {
    GetQueueUrlResponse urlResponse =
        client.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
    return urlResponse.queueUrl();
  }

  private void processMessage(String messageBody) {
    //log.info("DEBUG processMessage called with: {}", messageBody);
    try {
      JSONParser jsonParser = new JSONParser();
      JSONObject json = (JSONObject) jsonParser.parse(messageBody);
      //log.info("DEBUG parsed json, buoyId type: {}", json.get("buoyId").getClass().getName());
      BuoyResponse buoyResponse = parseBuoyResponse(json);
      dataStore.update(List.of(buoyResponse));
      int buoyId = ((Long) json.get("buoyId")).intValue();
      //log.info("DEBUG calling checkGeofence for buoyId: {}", buoyId);
      checkGeofence(buoyId);
    } catch (Exception e) {
      log.error("Error processing message: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void checkGeofence(int buoyId) {
    try {
      Optional<BuoyResponse> latestOpt = dataStore.getLatest(buoyId);
      //log.info("DEBUG checkGeofence: latestOpt present={}", latestOpt.isPresent());
      if (!latestOpt.isPresent()) {
        return;
      }
      Optional<Deployment> deploymentOpt = dataStore.getDeployment(buoyId);
      //log.info("DEBUG checkGeofence: deploymentOpt present={}", deploymentOpt.isPresent());
      if (!deploymentOpt.isPresent()) {
        return;
      }
      BuoyResponse latest = latestOpt.get();
      boolean outside =
          GeofenceService.isOutsideFence(
              deploymentOpt.get(), latest.getLatitude(), latest.getLongitude());
      //log.info("DEBUG checkGeofence: outside={}", outside);
      
      if (outside) {
        log.warn("ALERT: Buoy {} left geofence!", buoyId);

        deploymentOpt.ifPresent(d -> {
          //log.info("DEBUG: userId={}, sending notification", d.userId);
          sendNotificationToUser(d.userId, buoyId, "Buoy " + buoyId + " has drifted out of bounds!");
        });

      }
    } catch (Exception e) {
      log.error("Error in geofence check (non-fatal): {}", e.getMessage());
    }
  }

  private void sendNotificationToUser(String userId, int buoyId, String message) {
    try {
      //URL url = new URL("http://localhost:3000/api/user/notifications");
      String frontendUrl = System.getenv("FRONTEND_URL") != null
        ? System.getenv("FRONTEND_URL")
        : "http://localhost:3000";
      URL url = new URL(frontendUrl + "/api/user/notifications");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("x-internal-secret", System.getenv("INTERNAL_API_SECRET"));
      conn.setDoOutput(true);

      JSONObject body = new JSONObject();
      body.put("userId", userId);
      body.put("buoyId", buoyId);
      body.put("type", "GEOFENCE");
      body.put("message", message);

      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.toString().getBytes(StandardCharsets.UTF_8));
      }

      int status = conn.getResponseCode();
      //log.info("DEBUG notification POST status: {}", status);
      if (status != 201) {
        log.error("Failed to create notification, status: {}", status);
      }
      conn.disconnect();
    } catch (Exception e) {
      log.error("Error sending notification: {}", e.getMessage());
    }
  }

  private BuoyResponse parseBuoyResponse(JSONObject json) {
    int buoyId = ((Long) json.get("buoyId")).intValue();
    Object timeObj = json.get("timestamp");
    Instant timestamp;
    if (timeObj instanceof String) {
      timestamp = Instant.parse((String) timeObj);
    } else if (timeObj instanceof Long) {
      timestamp = Instant.ofEpochMilli((Long) timeObj);
    } else {
      timestamp = Instant.now();
    }
    double temperature = ((Number) json.get("temperature")).doubleValue();
    double pressure = ((Number) json.get("pressure")).doubleValue();
    double latitude = ((Number) json.get("latitude")).doubleValue();
    double longitude = ((Number) json.get("longitude")).doubleValue();
    return new BuoyResponse(buoyId, timestamp, temperature, pressure, latitude, longitude);
  }

  public void testProcessMessage(String body) {
    processMessage(body);
  }
}
