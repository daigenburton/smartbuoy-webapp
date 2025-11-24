package edu.bu.sqs;

import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

/**
 * QueueReader continuously polls SQS for new messages and processes them. Implements exponential
 * back-off strategy to avoid overwhelming the queue when empty.
 */
public class SQSQueueReader {
  private static final String SQS_QUEUE_NAME = "smartbuoy";
  private static final Region AWS_REGION = Region.US_EAST_1;
  SqsClient sqsClient =
      SqsClient.builder()
          .region(Region.US_EAST_1)
          .credentialsProvider(ProfileCredentialsProvider.create("default"))
          .build();

  // Sleep durations
  private static final long BASE_SLEEP_MS = 100; // Sleep when messages available
  private static final long INITIAL_BACKOFF_MS = 500; // First empty queue backoff
  private static final long MAX_BACKOFF_MS = 60000; // Max backoff (1 minute)
  private static final int BACKOFF_MULTIPLIER = 2; // Exponential multiplier
  private static final int SQS_WAIT_TIME_SECONDS = 10; // Long polling wait time

  final HttpClient httpClient;
  final DataStore dataStore;
  private long currentBackoffMs;

  /**
   * Creates a new QueueReader that will update the provided DataStore
   *
   * @param dataStore The DataStore to update with queue messages
   */
  public SQSQueueReader(DataStore dataStore) {
    this.dataStore = dataStore;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    this.currentBackoffMs = INITIAL_BACKOFF_MS;
  }

  /**
   * Starts the queue reader in a new background thread. This runs continuously and does not stop.
   */
  public void start() {
    new Thread(
            () -> {
              System.out.println("QueueReader started, polling SQS queue: " + SQS_QUEUE_NAME);

              while (true) {
                try {
                  pollQueue();
                } catch (InterruptedException e) {
                  System.out.println("QueueReader interrupted");
                  Thread.currentThread().interrupt();
                  break;
                } catch (Exception e) {
                  System.err.println("Error in QueueReader: " + e.getMessage());
                  e.printStackTrace();
                  // Sleep before retry on error
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                  }
                }
              }
            })
        .start();
  }

  /** Polls the SQS queue once and handles the response with exponential back-off */
  private void pollQueue() throws Exception {
    String queueUrl = getQueueUrl(sqsClient, SQS_QUEUE_NAME);

    ReceiveMessageRequest receiveMessageRequest =
        ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10) // Process up to 10 messages at once
            .waitTimeSeconds(SQS_WAIT_TIME_SECONDS) // Long polling
            .build();

    ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
    List<Message> messages = response.messages();

    if (!messages.isEmpty()) {
      // Successfully received messages
      System.out.println("Received " + messages.size() + " message(s) from SQS");

      for (Message message : messages) {
        try {
          processMessage(message.body());

          // Delete the message after successful processing
          deleteMessage(message.receiptHandle());

        } catch (Exception e) {
          System.err.println(
              "Error processing message " + message.messageId() + ": " + e.getMessage());
          // Message remains in queue and will be retried later
        }
      }

      // Reset backoff - more messages likely available
      currentBackoffMs = INITIAL_BACKOFF_MS;

      // Short sleep since more messages probably waiting
      Thread.sleep(BASE_SLEEP_MS);

    } else {
      // Queue is empty
      System.out.println("Queue empty, backing off for " + currentBackoffMs + "ms");

      // Sleep with current backoff duration
      Thread.sleep(currentBackoffMs);

      // Exponentially increase backoff for next empty response, up to max
      currentBackoffMs = Math.min(currentBackoffMs * BACKOFF_MULTIPLIER, MAX_BACKOFF_MS);
    }
  }

  /** Deletes a message from the queue after successful processing */
  private void deleteMessage(String receiptHandle) {
    String queueUrl = getQueueUrl(sqsClient, SQS_QUEUE_NAME);
    try {
      DeleteMessageRequest deleteMessageRequest =
          DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(receiptHandle).build();

      sqsClient.deleteMessage(deleteMessageRequest);

    } catch (SqsException e) {
      System.err.println("Failed to delete message: " + e.awsErrorDetails().errorMessage());
    }
  }

  private static String getQueueUrl(SqsClient sqsClient, String queueName) throws SqsException {
    GetQueueUrlResponse getQueueUrlResponse =
        sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
    return getQueueUrlResponse.queueUrl();
  }

  /**
   * Processes a message from the queue and updates the DataStore.
   *
   * @param messageBody The message body from the queue
   */
  private void processMessage(String messageBody) {
    try {
      JSONParser jsonParser = new JSONParser();
      JSONObject json = (JSONObject) jsonParser.parse(messageBody);

      String measurementType = (String) json.get("measurementType");
      double measurementVal = (double) json.get("measurementVal");
      int buoyId = ((Long) json.get("buoyId")).intValue();
      Object timeObj = json.get("msSinceEpoch");
      long msSinceEpoch =
          (timeObj instanceof String)
              ? java.time.Instant.parse((String) timeObj).toEpochMilli()
              : (long) timeObj;

      BuoyResponse response =
          new BuoyResponse(measurementType, measurementVal, buoyId, msSinceEpoch);

      dataStore.update(List.of(response));

      System.out.println("Data for buoy " + buoyId + ": " + dataStore.getHistory(buoyId));

    } catch (Exception e) {
      System.err.println("Error processing message: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /** Closes the SQS client when shutting down */
  public void shutdown() {
    if (sqsClient != null) {
      sqsClient.close();
      System.out.println("QueueReader shutdown complete");
    }
  }
}
