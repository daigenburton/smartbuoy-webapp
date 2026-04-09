package edu.bu.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

/** Maintains in-memory device shadow state and handles MQTT shadow messages. */
@Service
public class ShadowService {

  private static final Logger log = LoggerFactory.getLogger(ShadowService.class);

  private final Map<String, DeviceShadow> shadowState = new ConcurrentHashMap<>();

  /**
   * Nullable — only wired when mqtt.enabled=true and MqttConfig is active.
   * Publish calls are no-ops when MQTT is disabled.
   */
  @Nullable private final MessageChannel mqttOutboundChannel;

  private final ObjectMapper objectMapper;

  @Autowired
  public ShadowService(
      @Autowired(required = false) @Qualifier("nullChannel") MessageChannel mqttOutboundChannel,
      ObjectMapper objectMapper) {
    this.mqttOutboundChannel = mqttOutboundChannel;
    this.objectMapper = objectMapper;
  }

  /** Receives inbound shadow messages from AWS IoT Core via Spring Integration. */
  public void handleInbound(Message<String> message) {
    String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
    if (topic == null) {
      log.warn("Received MQTT message without topic header");
      return;
    }

    String buoyId = extractBuoyId(topic);
    if (buoyId == null) {
      log.warn("Could not extract buoy ID from topic: {}", topic);
      return;
    }

    String messageType = extractMessageType(topic);

    try {
      ShadowUpdateMessage update =
          objectMapper.readValue(message.getPayload(), ShadowUpdateMessage.class);
      processShadowUpdate(buoyId, messageType, update);
    } catch (Exception e) {
      log.error("Failed to parse shadow message on topic {}: {}", topic, e.getMessage());
    }
  }

  private void processShadowUpdate(
      String buoyId, String messageType, ShadowUpdateMessage update) {
    if ("rejected".equals(messageType)) {
      log.warn("Shadow update rejected for buoy {}", buoyId);
      return;
    }

    ShadowUpdateMessage.ShadowState state = update.getState();
    if (state == null) {
      return;
    }

    if ("delta".equals(messageType) && state.getDelta() != null) {
      ShadowUpdateMessage.ShadowFields delta = state.getDelta();
      log.info(
          "Buoy {} shadow delta: desired sampleIntervalSec={}",
          buoyId,
          delta.getSampleIntervalSec());
    }

    DeviceShadow existing = shadowState.get(buoyId);
    Integer battery = existing != null ? existing.battery() : null;
    String status = existing != null ? existing.status() : null;
    Integer desiredSampleInterval = existing != null ? existing.sampleIntervalSec() : null;
    Integer reportedSampleInterval = existing != null ? existing.reportedSampleIntervalSec() : null;

    ShadowUpdateMessage.ShadowFields reported = state.getReported();
    if (reported != null) {
      if (reported.getBattery() != null) battery = reported.getBattery();
      if (reported.getStatus() != null) status = reported.getStatus();
      if (reported.getSampleIntervalSec() != null)
        reportedSampleInterval = reported.getSampleIntervalSec();
    }

    ShadowUpdateMessage.ShadowFields desired = state.getDesired();
    if (desired != null && desired.getSampleIntervalSec() != null) {
      desiredSampleInterval = desired.getSampleIntervalSec();
    }

    shadowState.put(
        buoyId,
        new DeviceShadow(
            buoyId, battery, status, desiredSampleInterval, reportedSampleInterval, Instant.now()));
  }

  /**
   * Publishes a desired state update to the AWS IoT Core shadow for the given buoy.
   *
   * @param buoyId the thing name (e.g. "buoy-1")
   * @param desired the desired shadow fields to publish
   */
  public void publishDesiredState(String buoyId, ShadowUpdateMessage.ShadowFields desired) {
    if (mqttOutboundChannel == null) {
      log.warn("MQTT is disabled — skipping desired state publish for buoy {}", buoyId);
      return;
    }
    String topic = "$aws/things/" + buoyId + "/shadow/update";
    try {
      Map<String, Object> payload =
          Map.of("state", Map.of("desired", Map.of("sampleIntervalSec", desired.getSampleIntervalSec())));
      String json = objectMapper.writeValueAsString(payload);
      Message<String> msg =
          MessageBuilder.withPayload(json)
              .setHeader(MqttHeaders.TOPIC, topic)
              .build();
      mqttOutboundChannel.send(msg);
      log.info("Published desired state to {}: {}", topic, json);
    } catch (Exception e) {
      log.error("Failed to publish desired state for buoy {}: {}", buoyId, e.getMessage());
    }
  }

  public Optional<DeviceShadow> getShadow(String buoyId) {
    return Optional.ofNullable(shadowState.get(buoyId));
  }

  /** Extracts thing name (e.g. "buoy-1") from a shadow topic path. */
  private static String extractBuoyId(String topic) {
    // Topic format: $aws/things/{thingName}/shadow/update/{type}
    String[] parts = topic.split("/");
    if (parts.length >= 3 && "$aws".equals(parts[0]) && "things".equals(parts[1])) {
      return parts[2];
    }
    return null;
  }

  /** Extracts the message type suffix (delta/accepted/rejected) from a shadow topic. */
  private static String extractMessageType(String topic) {
    if (topic.endsWith("/delta")) return "delta";
    if (topic.endsWith("/accepted")) return "accepted";
    if (topic.endsWith("/rejected")) return "rejected";
    return "unknown";
  }
}
