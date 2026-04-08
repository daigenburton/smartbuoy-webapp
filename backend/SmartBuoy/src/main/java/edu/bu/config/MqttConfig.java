package edu.bu.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import edu.bu.shadow.ShadowService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/** Spring Integration configuration for AWS IoT Core MQTT device shadow messaging. */
@Configuration
@EnableIntegration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttConfig {

  private static final Logger log = LoggerFactory.getLogger(MqttConfig.class);

  private static final int KEEP_ALIVE_INTERVAL = 60;
  private static final int COMPLETION_TIMEOUT = 5000;
  private static final int DEFAULT_QOS = 1;

  @Value("${mqtt.endpoint}")
  private String endpoint;

  @Value("${mqtt.client-id}")
  private String clientId;

  @Value("${mqtt.cert-path}")
  private String certPath;

  @Value("${mqtt.key-path}")
  private String keyPath;

  @Value("${mqtt.root-ca-path}")
  private String rootCaPath;

  private final ShadowService shadowService;

  public MqttConfig(@Lazy ShadowService shadowService) {
    this.shadowService = shadowService;
  }

  private static final String[] SHADOW_TOPICS = {
    "$aws/things/buoy-1/shadow/update/delta",
    "$aws/things/buoy-1/shadow/update/accepted",
    "$aws/things/buoy-1/shadow/update/rejected",
    "$aws/things/buoy-2/shadow/update/delta",
    "$aws/things/buoy-2/shadow/update/accepted",
    "$aws/things/buoy-2/shadow/update/rejected",
    "$aws/things/buoy-3/shadow/update/delta",
    "$aws/things/buoy-3/shadow/update/accepted",
    "$aws/things/buoy-3/shadow/update/rejected",
  };

  private SSLContext buildSslContext() throws Exception {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");

    // Trust store: Root CA
    Certificate rootCa;
    try (FileInputStream caStream = new FileInputStream(rootCaPath)) {
      rootCa = cf.generateCertificate(caStream);
    }
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null, null);
    trustStore.setCertificateEntry("root-ca", rootCa);
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(trustStore);

    // Key store: device cert + private key
    Certificate deviceCert;
    try (FileInputStream certStream = new FileInputStream(certPath)) {
      deviceCert = cf.generateCertificate(certStream);
    }
    PrivateKey privateKey = loadPrivateKey(keyPath);
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, null);
    keyStore.setCertificateEntry("device-cert", deviceCert);
    char[] emptyPassword = new char[0];
    keyStore.setKeyEntry("device-key", privateKey, emptyPassword, new Certificate[] {deviceCert});
    KeyManagerFactory kmf =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, emptyPassword);

    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    return sslContext;
  }

  /** Parses a PKCS8 PEM private key file without BouncyCastle. */
  private static PrivateKey loadPrivateKey(String path) throws Exception {
    String pem;
    try (FileInputStream fis = new FileInputStream(path)) {
      pem = new String(fis.readAllBytes());
    }
    String stripped =
        pem.replaceAll("-----BEGIN (.*)-----", "")
            .replaceAll("-----END (.*)-----", "")
            .replaceAll("\\s", "");
    byte[] keyBytes = Base64.getDecoder().decode(stripped);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }

  @Bean
  public MqttConnectOptions mqttConnectOptions() {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setServerURIs(new String[] {"ssl://" + endpoint + ":8883"});
    options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
    options.setCleanSession(false);
    options.setAutomaticReconnect(true);
    try {
      options.setSocketFactory(buildSslContext().getSocketFactory());
    } catch (IOException e) {
      log.warn("MQTT cert files not found — MQTT will not connect: {}", e.getMessage());
    } catch (Exception e) {
      log.warn("Failed to build MQTT SSL context: {}", e.getMessage());
    }
    return options;
  }

  @Bean
  public DefaultMqttPahoClientFactory mqttClientFactory() {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    factory.setConnectionOptions(mqttConnectOptions());
    return factory;
  }

  @Bean
  public MessageChannel mqttInboundChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mqttOutboundChannel() {
    return new DirectChannel();
  }

  @Bean
  public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
    MqttPahoMessageDrivenChannelAdapter adapter =
        new MqttPahoMessageDrivenChannelAdapter(
            clientId + "-inbound", mqttClientFactory(), SHADOW_TOPICS);
    adapter.setCompletionTimeout(COMPLETION_TIMEOUT);
    adapter.setConverter(new DefaultPahoMessageConverter());
    adapter.setQos(DEFAULT_QOS);
    adapter.setOutputChannel(mqttInboundChannel());
    return adapter;
  }

  @Bean
  @ServiceActivator(inputChannel = "mqttInboundChannel")
  public MessageHandler mqttInboundHandler() {
    return message -> shadowService.handleInbound((org.springframework.messaging.Message<String>) message);
  }

  @Bean
  @ServiceActivator(inputChannel = "mqttOutboundChannel")
  public MessageHandler mqttOutboundHandler() {
    MqttPahoMessageHandler handler =
        new MqttPahoMessageHandler(clientId + "-outbound", mqttClientFactory());
    handler.setAsync(true);
    handler.setDefaultQos(DEFAULT_QOS);
    return handler;
  }
}
