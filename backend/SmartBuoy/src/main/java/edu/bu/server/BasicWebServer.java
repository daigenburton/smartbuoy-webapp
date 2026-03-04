package edu.bu.server;

import com.sun.net.httpserver.HttpServer;
import edu.bu.data.DataStore;
import edu.bu.server.handlers.CurrentHandler;
import edu.bu.server.handlers.DeploymentHandler;
import edu.bu.server.handlers.HistoryHandler;
import edu.bu.server.handlers.LatestMeasurementHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.tinylog.Logger;

/**
 * Registers RESTful HTTP endpoints that are supported by the StockApp. These endpoints will be
 * invoked by StockApp users to retrieve analytics computed by StockApp.
 */
public class BasicWebServer {
  final DataStore store;
  private HttpServer server;

  public BasicWebServer(DataStore store) {
    this.store = store;
  }

  public void start() throws IOException {
    // Create an HttpServer instance
    server = HttpServer.create(new InetSocketAddress(8000), 0);

    // Create handler for history requests for individual buoyIds
    server.createContext("/history", new HistoryHandler(store));

    // Create handler for current requests for individual buoyIds
    server.createContext("/current", new CurrentHandler(store));

    // Create handlers for specfic data requests for individual buoyIds
    server.createContext("/temperature", new LatestMeasurementHandler(store, "temperature"));
    server.createContext("/pressure", new LatestMeasurementHandler(store, "pressure"));
    server.createContext("/location", new LatestMeasurementHandler(store, "location"));

    // Create handler for buoy deployment
    server.createContext("/deploy", new DeploymentHandler(store));

    // server.createContext("/update", new UpdateHandler(store));

    // Start the server
    server.setExecutor(null); // Use the default executor
    server.start();

    Logger.info("Server is running on port 8000");
  }

  public void stop() {
    if (server != null) {
      server.stop(0);
    }
  }
}
