package edu.bu.data;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import edu.bu.analytics.UnknownBuoyException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Persistent data store used to hold buoy readings with InfluxDB */
public class InfluxDBStore implements DataStore {

  private final InfluxDBClient client;
  private final String bucket;
  private final String org;

  /** Constructor with connection parameters */
  public InfluxDBStore(String url, String token, String org, String bucket) {
    this.client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    this.bucket = bucket;
    this.org = org;
  }

  /** Default constructor using values from docker-compose */
  public InfluxDBStore() {
    this(
        "http://influxdb:8086",
        "api-key-ask-daigen",
        "smart-buoy",
        "device-data");
  }

  @Override
  public void update(List<BuoyResponse> responses) {
    WriteApiBlocking writeApi = client.getWriteApiBlocking();

    for (BuoyResponse response : responses) {
      String lineProtocol = createLineProtocol(response);
      writeApi.writeRecord(bucket, org, WritePrecision.NS, lineProtocol);
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

  /** Create InfluxDB line protocol string from BuoyResponse */
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

  /** Build Flux query for historical data */
  private String buildHistoryQuery(int buoyId) {
    return String.format(
        "from(bucket: \"%s\") "
            + "|> range(start: -30d) "
            + "|> filter(fn: (r) => r[\"_measurement\"] == \"buoy_data\") "
            + "|> filter(fn: (r) => r[\"buoy_id\"] == \"%d\")",
        bucket, buoyId);
  }

  /** Build Flux query for latest data */
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

  /** Execute a Flux query and return results */
  private List<FluxTable> executeQuery(String flux) {
    QueryApi queryApi = client.getQueryApi();
    return queryApi.query(flux, org);
  }

  /** Group flux records by timestamp */
  private Map<Instant, Map<String, Double>> groupRecordsByTimestamp(List<FluxTable> tables) {
    Map<Instant, Map<String, Double>> groupedData = new HashMap<>();

    for (FluxTable table : tables) {
      for (FluxRecord record : table.getRecords()) {
        Instant timestamp = record.getTime();
        String field = (String) record.getField();
        Double value = ((Number) record.getValue()).doubleValue();

        groupedData.computeIfAbsent(timestamp, k -> new HashMap<>()).put(field, value);
      }
    }

    return groupedData;
  }

  /** Extract field values from flux tables and return timestamp */
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

  /** Convert grouped data to sorted list of BuoyResponse objects */
  private List<BuoyResponse> convertToSortedBuoyResponses(
      Map<Instant, Map<String, Double>> groupedData, int buoyId) {
    return groupedData.entrySet().stream()
        .filter(entry -> entry.getValue().size() == 4)
        .map(entry -> createBuoyResponse(buoyId, entry.getKey(), entry.getValue()))
        .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
        .collect(Collectors.toList());
  }

  /** Create a BuoyResponse from field values */
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

  /** Clean up connection when done */
  public void close() {
    if (client != null) {
      client.close();
    }
  }
}
