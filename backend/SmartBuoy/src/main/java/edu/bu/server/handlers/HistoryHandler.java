package edu.bu.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.bu.analytics.UnknownBuoyException;
import edu.bu.data.BuoyResponse;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tinylog.Logger;

public class HistoryHandler implements HttpHandler {

  final DataStore dataStore;

  public HistoryHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    // parse out symbol of interest from URL
    String[] requestURLParts = exchange.getRequestURI().getRawPath().split("/");
    String strID =
        requestURLParts[requestURLParts.length - 1]; // api endpoint has to be /history/{buoyId}

    JSONObject json = new JSONObject();
    int statusCode;

    try {
      int buoyId = Integer.parseInt(strID);
      List<BuoyResponse> history = dataStore.getHistory(buoyId);

      JSONArray array = new JSONArray();
      for (BuoyResponse r : history) {
        JSONObject obj = new JSONObject();
        obj.put("buoyId", r.buoyId);
        obj.put("measurementType", r.measurementType);
        obj.put("value", r.measurementVal);
        obj.put("timestamp", r.msSinceEpoch);
        array.add(obj);
      }

      json.put("history", array);
      statusCode = 200;

    } catch (UnknownBuoyException e) {
      json.put("error", e.getMessage());
      statusCode = 404;
    }

    String response = json.toString();

    Logger.info("Handled history request for {}, responding with {}.", strID, response);

    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, response.length());

    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(response.getBytes());
    }
  }
}
