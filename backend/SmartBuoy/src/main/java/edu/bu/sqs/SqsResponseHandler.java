package edu.bu.sqs;

import edu.bu.ResponseHandler;
import java.io.IOException;
import org.json.simple.JSONObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

/** Handler for enqueueing finnhub responses to SQS service. */
public class SqsResponseHandler implements ResponseHandler {
  private static final String SQS_QUEUE_NAME = "smartbuoy";
  private static final Region AWS_REGION = Region.US_EAST_1;

  private final SqsClient sqsClient;
  private final String queueUrl;

  public SqsResponseHandler() {
    this.sqsClient = SqsClient.builder().region(AWS_REGION).build();
    this.queueUrl = getOrCreateQueue(sqsClient, SQS_QUEUE_NAME);
  }

  @Override
  public void enqueue(String message) throws IOException, InterruptedException {
    String jsonString = parseResponseToJson(message);
    System.out.println("Sending JSON: " + jsonString);

    try {
      System.out.println("\nWriting Finnhub Response to SQS");

      SendMessageRequest sendMessageRequest =
          SendMessageRequest.builder().queueUrl(queueUrl).messageBody(jsonString).build();

      SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
      System.out.println("Message sent successfully!");
      System.out.println("Message ID: " + response.messageId());
      System.out.println("Queue URL: " + queueUrl);

    } catch (SqsException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      throw e;
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

  private String parseResponseToJson(String message) {
    String dataString = message.substring(message.indexOf("{") + 1, message.length() - 1);
    JSONObject json = new JSONObject();
    String[] pairs = dataString.split(", ");

    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      String key = keyValue[0].trim();
      String value = keyValue[1].trim().replace("'", "");

      try {
        if (value.contains(".")) {
          json.put(key, Double.parseDouble(value));
        } else {
          json.put(key, Integer.parseInt(value));
        }
      } catch (NumberFormatException e) {
        json.put(key, value);
      }
    }

    return json.toString();
  }
}
