package edu.bu.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import edu.bu.shadow.ShadowService;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mqtt.core.ClientManager;
import org.springframework.integration.mqtt.core.Mqttv3ClientManager;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
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

  @Value("${mqtt.thing-name:esp32}")
  private String thingName;

  private final ShadowService shadowService;

  public MqttConfig(@Lazy ShadowService shadowService) {
    this.shadowService = shadowService;
  }

  /**
   * Builds an SNI-aware SSLSocketFactory for AWS IoT Core.
   *
   * <p>Paho MQTT v3 resolves the hostname to an InetAddress before creating the socket, losing the
   * hostname string. Without it, the JVM cannot include the SNI extension in the TLS ClientHello.
   * AWS IoT Core requires SNI and drops the connection without it (EOFException). This wrapper
   * re-injects the endpoint hostname into every socket so SNI is always sent.
   */
  private SSLSocketFactory buildSslSocketFactory() throws Exception {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");

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

    Certificate deviceCert;
    try (FileInputStream certStream = new FileInputStream(certPath)) {
      deviceCert = cf.generateCertificate(certStream);
    }
    if (deviceCert instanceof X509Certificate x509) {
      log.info("MQTT cert: subject={}, issuer={}, expires={}, serial={}",
          x509.getSubjectX500Principal(),
          x509.getIssuerX500Principal(),
          x509.getNotAfter(),
          x509.getSerialNumber().toString(16));
    }
    PrivateKey privateKey = loadPrivateKey(keyPath);
    log.info("MQTT private key algorithm: {}", privateKey.getAlgorithm());
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

    SSLSocketFactory base = sslContext.getSocketFactory();
    String sniHost = endpoint;
    return new SSLSocketFactory() {
      private SSLSocket withSni(Socket raw, String host) {
        SSLSocket s = (SSLSocket) raw;
        SSLParameters p = s.getSSLParameters();
        p.setServerNames(List.of(new SNIHostName(host)));
        s.setSSLParameters(p);
        s.addHandshakeCompletedListener(event ->
            log.info("TLS handshake COMPLETED: cipher={}, protocol={}", event.getCipherSuite(), event.getSession().getProtocol()));
        return s;
      }

      @Override public String[] getDefaultCipherSuites() { return base.getDefaultCipherSuites(); }
      @Override public String[] getSupportedCipherSuites() { return base.getSupportedCipherSuites(); }

      // Paho v3 calls createSocket() then socket.connect(addr, timeout) separately.
      // SNI must be set before connect(), so we apply it here on the unconnected socket.
      @Override
      public Socket createSocket() throws IOException {
        return withSni(base.createSocket(), sniHost);
      }

      @Override
      public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return withSni(base.createSocket(s, host, port, autoClose), host);
      }

      @Override
      public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return withSni(base.createSocket(host, port), host);
      }

      @Override
      public Socket createSocket(String host, int port, InetAddress local, int localPort) throws IOException, UnknownHostException {
        return withSni(base.createSocket(host, port, local, localPort), host);
      }

      // Called by Paho v3 after resolving hostname to InetAddress — hostname is lost here,
      // so we substitute the configured endpoint for SNI.
      @Override
      public Socket createSocket(InetAddress addr, int port) throws IOException {
        return withSni(base.createSocket(addr, port), sniHost);
      }

      @Override
      public Socket createSocket(InetAddress addr, int port, InetAddress local, int localPort) throws IOException {
        return withSni(base.createSocket(addr, port, local, localPort), sniHost);
      }
    };
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
    options.setCleanSession(true);
    // automaticReconnect=true lets Paho handle reconnect with built-in exponential backoff
    // (1s → 2s → 4s → ... → 128s). The Mqttv3ClientManager uses a single shared connection,
    // so there is only one reconnect happening at a time — no more dual-client storm.
    options.setAutomaticReconnect(true);
    try {
      options.setSocketFactory(buildSslSocketFactory());
    } catch (IOException e) {
      log.warn("MQTT cert files not found — MQTT will not connect: {}", e.getMessage());
    } catch (Exception e) {
      log.warn("Failed to build MQTT SSL context: {}", e.getMessage());
    }
    return options;
  }

  /**
   * Shared MQTT connection manager. Both the inbound adapter and outbound handler reuse this
   * single IMqttAsyncClient, which halves the number of TLS connections to AWS IoT Core.
   */
  @Bean
  public ClientManager<IMqttAsyncClient, MqttConnectOptions> mqttClientManager() {
    return new Mqttv3ClientManager(mqttConnectOptions(), clientId);
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
    String[] topics = {
      "$aws/things/" + thingName + "/shadow/update/delta",
      "$aws/things/" + thingName + "/shadow/update/accepted",
      "$aws/things/" + thingName + "/shadow/update/rejected",
      "$aws/things/" + thingName + "/shadow/get/accepted",
    };
    MqttPahoMessageDrivenChannelAdapter adapter =
        new MqttPahoMessageDrivenChannelAdapter(mqttClientManager(), topics);
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

  /**
   * Fires the shadow GET exactly once the inbound adapter has successfully subscribed.
   * This avoids the polling storm: the outbound publish only fires AFTER we know AWS IoT
   * is accepting our certificate, and uses the same shared connection as the inbound adapter.
   */
  @EventListener(MqttSubscribedEvent.class)
  public void onMqttSubscribed(MqttSubscribedEvent event) {
    log.info("shadow-init: MQTT subscribed ({}), requesting initial shadow", event.getMessage());
    for (int attempt = 1; attempt <= 5; attempt++) {
      if (shadowService.requestShadow(thingName)) return;
      log.warn("shadow-init: shadow GET failed on attempt {}, retrying in 2s", attempt);
      try { Thread.sleep(2000); } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
    log.error("shadow-init: shadow GET failed after 5 attempts");
  }

  @Bean
  @ServiceActivator(inputChannel = "mqttOutboundChannel")
  public MessageHandler mqttOutboundHandler() {
    MqttPahoMessageHandler handler = new MqttPahoMessageHandler(mqttClientManager());
    handler.setAsync(true);
    handler.setDefaultQos(DEFAULT_QOS);
    return handler;
  }
}
