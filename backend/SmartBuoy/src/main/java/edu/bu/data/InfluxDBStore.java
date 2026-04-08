package edu.bu.data;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import edu.bu.analytics.UnknownBuoyException;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Persistent data store backed by InfluxDB for time-series buoy readings. */
@Service
@ConditionalOnProperty(name = "influxdb.enabled", havingValue = "true")
public class InfluxDBStore implements DataStore {

  private final InfluxDBClient client;
  private final String bucket;
  private final String org;
  private final QueryApi queryApi;
  private final WriteApiBlocking writeApi;
  private final Map<Integer, Deployment> deployments = new ConcurrentHashMap<>();

  /** Creates an InfluxDBStore with connection parameters from application properties. */
  public InfluxDBStore(
      @Value("${influxdb.url}") String url,
      @Value("${influxdb.token}") String token,
      @Value("${influxdb.org}") String influxOrg,
      @Value("${influxdb.bucket}") String influxBucket) {
    this.client = InfluxDBClientFactory.create(url, token.toCharArray(), influxOrg, influxBucket);
    this.bucket = influxBucket;
    this.org = influxOrg;
    this.queryApi = client.getQueryApi();
    this.writeApi = client.getWriteApiBlocking();
  }

  @Override
  public void update(List<BuoyResponse> responses) {
    for (BuoyResponse response : responses) {
      writeApi.writeRecord(bucket, org, WritePrecision.NS, createLineProtocol(response));
    }
  }

  @Override
  public List<BuoyResponse> getHistory(int buoyId) throws UnknownBuoyException {
    String flux = buildHistoryQuery(buoyId);
    List<FluxTable> tables = executeQuery(flux);
    if (tables.isEmpty()) {
      throw new UnknownBuoyException(buoyId);
    }
    Map<Instant, Map<String, Double>> groupedData = groupRecordsByTimestamp(tables);
    if (groupedData.isEmpty()) {
      throw new UnknownBuoyException(buoyId);
    }
    return convertToSortedBuoyResponses(groupedData, buoyId);
  }

  @Override
  public Optional<BuoyResponse> getLatest(int buoyId) throws UnknownBuoyException {
    String flux = buildLatestQuery(buoyId);
    List<FluxTable> tables = executeQuery(flux);
    if (tables.isEmpty()) {
      return Optional.empty();
    }
    Map<String, Double> values = new HashMap<>();
    Instant timestamp = extractFieldValues(tables, values);
    if (values.size() != 4 || timestamp == null) {
      return Optional.empty();
    }
    return Optional.of(createBuoyResponse(buoyId, timestamp, values));
  }

  @Override
  public void saveDeployment(Deployment deployment) {
    deployments.put(deployment.buoyId, deployment);
  }

  @Override
  public Optional<Deployment> getDeployment(int buoyId) {
    return Optional.ofNullable(deployments.get(buoyId));
  }

  /** Closes the InfluxDB client on shutdown. */
  @PreDestroy
  public void close() {
    if (client != null) {
      client.close();
    }
  }

  private String createLineProtocol(BuoyResponse response) {
    return String.format(
        "buoy_data,buoy_id=%d temperature=%f,pressure=%f,latitude=%f,longitude=%f %d",
        response.getBuoyId(),
        response.getTemperature(),
        response.getPressure(),
        response.getLatitude(),
        response.getLongitude(),
        response.getTimestamp().toEpochMilli() * 1_000_000);
  }

  private String buildHistoryQuery(int buoyId) {
    return String.format(
        "from(bucket: \"%s\") "
            + "|> range(start: -30d) "
            + "|> filter(fn: (r) => r[\"_measurement\"] == \"buoy_data\") "
            + "|> filter(fn: (r) => r[\"buoy_id\"] == \"%d\")",
        bucket, buoyId);
  }

  private String buildLatestQuery(int buoyId) {
    return String.format(
        "from(bucket: \"%s\") "
            + "|> range(start: -30d) "
            + "|> filter(fn: (r) => r[\"_measurement\"] == \"buoy_data\") "
            + "|> filter(fn: (r) => r[\"buoy_id\"] == \"%d\") "
            + "|> sort(columns: [\"_time\"], desc: true) "
            + "|> limit(n: 1)",
        bucket, buoyId);
  }

  private List<FluxTable> executeQuery(String flux) {
    return queryApi.query(flux, org);
  }

  private Map<Instant, Map<String, Double>> groupRecordsByTimestamp(List<FluxTable> tables) {
    Map<Instant, Map<String, Double>> groupedData = new HashMap<>();
    for (FluxTable table : tables) {
      for (FluxRecord record : table.getRecords()) {
        Instant timestamp = record.getTime();
        String field = (String) record.getField();
        Double value = ((Number) record.getValue()).doubleValue();
        groupedData.computeIfAbsent(timestamp, ignored -> new HashMap<>()).put(field, value);
      }
    }
    return groupedData;
  }

  private Instant extractFieldValues(List<FluxTable> tables, Map<String, Double> values) {
    Instant timestamp = null;
    for (FluxTable table : tables) {
      for (FluxRecord record : table.getRecords()) {
        if (timestamp == null) {
          timestamp = record.getTime();
        }
        String field = (String) record.getField();
        Double value = ((Number) record.getValue()).doubleValue();
        values.put(field, value);
      }
    }
    return timestamp;
  }

  private List<BuoyResponse> convertToSortedBuoyResponses(
      Map<Instant, Map<String, Double>> groupedData, int buoyId) {
    return groupedData.entrySet().stream()
        .filter(entry -> entry.getValue().size() == 4)
        .map(entry -> createBuoyResponse(buoyId, entry.getKey(), entry.getValue()))
        .sorted((alpha, beta) -> alpha.getTimestamp().compareTo(beta.getTimestamp()))
        .collect(Collectors.toList());
  }

  private BuoyResponse createBuoyResponse(
      int buoyId, Instant timestamp, Map<String, Double> values) {
    return new BuoyResponse(
        buoyId,
        timestamp,
        values.get("temperature"),
        values.get("pressure"),
        values.get("latitude"),
        values.get("longitude"));
  }
}
