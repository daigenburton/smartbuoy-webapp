package edu.bu.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

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
      @Autowired(required = false) @Qualifier("mqttOutboundChannel") MessageChannel mqttOutboundChannel,
      ObjectMapper objectMapper) {
    this.mqttOutboundChannel = mqttOutboundChannel;
    this.objectMapper = objectMapper;
  }

  /** Receives inbound shadow messages from AWS IoT Core via Spring Integration. */
  public void handleInbound(Message<String> message) {
    String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
    log.info("MQTT inbound: topic={}", topic);
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
      log.info("Buoy {} shadow delta: led={}, buzzer={}, deployed={}", buoyId, delta.getLed(), delta.getBuzzer(), delta.getDeployed());
    }

    DeviceShadow existing = shadowState.get(buoyId);
    Integer battery = existing != null ? existing.battery() : null;
    Boolean led = existing != null ? existing.led() : null;
    Boolean buzzer = existing != null ? existing.buzzer() : null;
    Boolean deployed = existing != null ? existing.deployed() : null;

    ShadowUpdateMessage.ShadowFields reported = state.getReported();
    if (reported != null) {
      if (reported.getBattery() != null) battery = reported.getBattery();
      if (reported.getLed() != null) led = reported.getLed();
      if (reported.getBuzzer() != null) buzzer = reported.getBuzzer();
      if (reported.getDeployed() != null) deployed = reported.getDeployed();
    }

    ShadowUpdateMessage.ShadowFields desired = state.getDesired();
    if (desired != null) {
      if (desired.getLed() != null) led = desired.getLed();
      if (desired.getBuzzer() != null) buzzer = desired.getBuzzer();
      if (desired.getDeployed() != null) deployed = desired.getDeployed();
    }

    shadowState.put(
        buoyId,
        new DeviceShadow(buoyId, battery, led, buzzer, deployed, Instant.now()));
  }

  /**
   * Publishes a desired state update to the AWS IoT Core shadow for the given buoy.
   *
   * @param buoyId the thing name (e.g. "esp32")
   * @param desired the desired shadow fields to publish
   */
  public void publishDesiredState(String buoyId, ShadowUpdateMessage.ShadowFields desired) {
    if (mqttOutboundChannel == null) {
      log.warn("MQTT is disabled — skipping desired state publish for buoy {}", buoyId);
      return;
    }
    String topic = "$aws/things/" + buoyId + "/shadow/update";
    try {
      Map<String, Object> desiredMap = new java.util.HashMap<>();

      if (desired.getLed() != null) {
        desiredMap.put("led", desired.getLed());
      }
      if (desired.getBuzzer() != null) {
        desiredMap.put("buzzer", desired.getBuzzer());
      }
      if (desired.getDeployed() != null) {
        desiredMap.put("deployed", desired.getDeployed());
      }

      Map<String, Object> payload = Map.of("state", Map.of("desired", desiredMap));

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

  /**
   * Publishes an empty get request so AWS IoT responds with the current shadow state.
   *
   * @return true if published successfully, false if MQTT is unavailable or publish failed
   */
  public boolean requestShadow(String buoyId) {
    if (mqttOutboundChannel == null) return false;
    String topic = "$aws/things/" + buoyId + "/shadow/get";
    try {
      Message<String> msg = MessageBuilder.withPayload("{}").setHeader(MqttHeaders.TOPIC, topic).build();
      mqttOutboundChannel.send(msg);
      log.info("Requested shadow state for {}", buoyId);
      return true;
    } catch (Exception e) {
      log.warn("Shadow request failed for {}", buoyId, e);
      return false;
    }
  }

  /** Extracts thing name from a shadow topic path. */
  private static String extractBuoyId(String topic) {
    // Topic format: $aws/things/{thingName}/shadow/update/{type}
    String[] parts = topic.split("/");
    if (parts.length >= 3 && "$aws".equals(parts[0]) && "things".equals(parts[1])) {
      return parts[2];
    }
    return null;
  }

  /** Extracts the message type suffix from a shadow topic. */
  private static String extractMessageType(String topic) {
    if (topic.endsWith("/delta")) return "delta";
    if (topic.endsWith("/accepted")) return "accepted";
    if (topic.endsWith("/rejected")) return "rejected";
    return "unknown";
  }
}
