package edu.bu.sqs;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

@Tag("Probe")
public class A_SQSWriteProbe {

  private static final String QUEUE_NAME = "smartbuoy";
  private static final Region AWS_REGION = Region.US_EAST_1;

  @Test
  public void writeSingleMessage() {
    try {
      System.out.println("\nWriting Single Message");
      SqsClient sqsClient = SqsClient.builder().region(AWS_REGION).build();

      String queueUrl = getOrCreateQueue(sqsClient, QUEUE_NAME);

      SendMessageRequest sendMessageRequest =
          SendMessageRequest.builder()
              .queueUrl(queueUrl)
              .messageBody("WriteProbe - Single Message")
              .build();

      SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
      System.out.println("Message sent successfully!");
      System.out.println("Message ID: " + response.messageId());
      System.out.println("Queue URL: " + queueUrl);

      sqsClient.close();

    } catch (SqsException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      throw e;
    }
  }

  @Test
  public void writeMultipleMessages() {
    try {
      System.out.println("Writing Multiple Messages");
      SqsClient sqsClient = SqsClient.builder().region(AWS_REGION).build();

      String queueUrl = getOrCreateQueue(sqsClient, QUEUE_NAME);

      int messageCount = 5;
      for (int i = 1; i <= messageCount; i++) {
        SendMessageRequest sendMessageRequest =
            SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody("Message #" + i + " - Timestamp: " + System.currentTimeMillis())
                .build();

        SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
        System.out.println(
            "Sent message " + i + "/" + messageCount + " - ID: " + response.messageId());

        // Small delay to see messages appearing in AWS console graphs
        Thread.sleep(500);
      }

      System.out.println("\nAll " + messageCount + " messages sent successfully!");

      sqsClient.close();

    } catch (SqsException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      throw e;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  public static String getOrCreateQueue(SqsClient sqsClient, String queueName) {
    try {
      // Try to get existing queue first
      GetQueueUrlResponse getQueueUrlResponse =
          sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
      System.out.println("Using existing queue: " + queueName);
      return getQueueUrlResponse.queueUrl();

    } catch (QueueDoesNotExistException e) {
      // Queue doesn't exist, create it
      System.out.println("Creating new queue: " + queueName);
      CreateQueueRequest createQueueRequest =
          CreateQueueRequest.builder().queueName(queueName).build();
      sqsClient.createQueue(createQueueRequest);

      GetQueueUrlResponse getQueueUrlResponse =
          sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
      System.out.println("Queue created: " + getQueueUrlResponse.queueUrl());
      return getQueueUrlResponse.queueUrl();

    } catch (SqsException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      throw e;
    }
  }
}
