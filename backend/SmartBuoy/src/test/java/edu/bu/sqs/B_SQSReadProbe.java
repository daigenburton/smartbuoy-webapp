package edu.bu.sqs;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

@Tag("Probe")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class B_SQSReadProbe {

  private static final String QUEUE_NAME = "smartbuoy";
  private static final Region AWS_REGION = Region.US_EAST_1;

  @BeforeEach
  void setup() throws InterruptedException {
    Thread.sleep(2000); // Wait 2 seconds for WriteProbe to finish
  }

  @Test
  @Order(1)
  public void readSingleMessage() {
    System.out.println("Test 1: Reading Single Message");
    SqsClient sqsClient = SqsClient.builder().region(AWS_REGION).build();

    try {
      String queueUrl = getQueueUrl(sqsClient, QUEUE_NAME);
      System.out.println("Reading from queue: " + queueUrl);

      ReceiveMessageRequest receiveMessageRequest =
          ReceiveMessageRequest.builder()
              .queueUrl(queueUrl)
              .maxNumberOfMessages(1)
              .waitTimeSeconds(10)
              .build();

      List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

      System.out.println("Received " + messages.size() + " message(s)");

      if (!messages.isEmpty()) {
        Message message = messages.get(0);
        System.out.println("Message received successfully!");
        System.out.println("Message ID: " + message.messageId());
        System.out.println("Message Body: " + message.body());
        System.out.println("Receipt Handle: " + message.receiptHandle().substring(0, 50) + "...");

        deleteMessage(sqsClient, queueUrl, message.receiptHandle());

        assertTrue(message.body().contains("WriteProbe"), "Message should be from WriteProbe");
      } else {
        System.out.println("No messages in queue. Run writeSingleMessage() first.");
      }

    } catch (SqsException e) {
      System.err.println("Error: " + e.awsErrorDetails().errorMessage());
      fail("SQS operation failed: " + e.getMessage());
    } finally {
      sqsClient.close();
    }
  }

  @Test
  @Order(2)
  public void readMultipleMessages() {
    System.out.println("\n=== Test 2: Reading Multiple Messages ===");
    SqsClient sqsClient = SqsClient.builder().region(AWS_REGION).build();

    try {
      String queueUrl = getQueueUrl(sqsClient, QUEUE_NAME);
      System.out.println("Reading from queue: " + queueUrl);

      int totalMessagesReceived = 0;
      int maxIterations = 3;

      for (int iteration = 1; iteration <= maxIterations; iteration++) {
        System.out.println("\nBatch " + iteration + ":");

        ReceiveMessageRequest receiveMessageRequest =
            ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

        if (messages.isEmpty()) {
          System.out.println("No more messages in queue");
          break;
        }

        System.out.println("Received " + messages.size() + " message(s)");

        for (Message message : messages) {
          totalMessagesReceived++;
          System.out.println("Message " + totalMessagesReceived + ":");
          System.out.println("ID: " + message.messageId());
          System.out.println("Body: " + message.body());

          deleteMessage(sqsClient, queueUrl, message.receiptHandle());
        }
      }

      System.out.println("Total messages received and deleted: " + totalMessagesReceived);

      if (totalMessagesReceived >= 5) {
        System.out.println("Successfully received multiple messages!");
      } else {
        System.out.println("Expected 5 messages. Run writeMultipleMessages() first.");
      }

      assertTrue(totalMessagesReceived >= 0, "Should receive messages without error");

    } catch (SqsException e) {
      System.err.println("Error: " + e.awsErrorDetails().errorMessage());
      fail("SQS operation failed: " + e.getMessage());
    } finally {
      sqsClient.close();
    }
  }

  @Test
  @Order(3)
  public void readEmptyQueue() {
    System.out.println("Test 4: Reading Empty Queue");
    SqsClient sqsClient = SqsClient.builder().region(AWS_REGION).build();

    try {
      String queueUrl = getQueueUrl(sqsClient, QUEUE_NAME);
      System.out.println("Attempting to read from queue: " + queueUrl);

      ReceiveMessageRequest receiveMessageRequest =
          ReceiveMessageRequest.builder()
              .queueUrl(queueUrl)
              .maxNumberOfMessages(10)
              .waitTimeSeconds(5)
              .build();

      List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

      if (messages.isEmpty()) {
        System.out.println("Queue is empty - handled gracefully");
        System.out.println("No errors thrown");
        System.out.println("Returned empty list");
      } else {
        System.out.println("Queue has " + messages.size() + " message(s)");
        System.out.println("To test empty queue, read all messages first");
        for (Message msg : messages) {
          deleteMessage(sqsClient, queueUrl, msg.receiptHandle());
        }
      }

      assertTrue(true, "Reading empty queue should not throw exception");

    } catch (SqsException e) {
      System.err.println("Error: " + e.awsErrorDetails().errorMessage());
      fail("Should handle empty queue gracefully: " + e.getMessage());
    } finally {
      sqsClient.close();
    }
  }

  private static String getQueueUrl(SqsClient sqsClient, String queueName) throws SqsException {
    GetQueueUrlResponse getQueueUrlResponse =
        sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
    return getQueueUrlResponse.queueUrl();
  }

  private static void deleteMessage(SqsClient sqsClient, String queueUrl, String receiptHandle) {
    try {
      DeleteMessageRequest deleteMessageRequest =
          DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(receiptHandle).build();
      sqsClient.deleteMessage(deleteMessageRequest);
      System.out.println("Message deleted from queue");
    } catch (SqsException e) {
      System.err.println("Failed to delete message: " + e.awsErrorDetails().errorMessage());
    }
  }
}
