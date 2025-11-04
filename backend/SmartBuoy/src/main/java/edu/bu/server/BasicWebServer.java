package edu.bu.server;

import com.sun.net.httpserver.HttpServer;
import edu.bu.data.DataStore;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.tinylog.Logger;

/**
 * Registers RESTful HTTP endpoints that are supported by the StockApp. These endpoints will be
 * invoked by StockApp users to retrieve analytics computed by StockApp.
 */
public class BasicWebServer {
  final DataStore store;

  public BasicWebServer(DataStore store) {
    this.store = store;
  }

  public void start() throws IOException {
    // Create an HttpServer instance
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

    // Create handler for price requests for individual symbols
    //        server.createContext(
    //                "/price", new PriceHandler(analyticsComputor, metricsTracker));
    //
    //        // Create handler for listing of all known symbols
    //        server.createContext("/symbols", new SymbolListHandler(analyticsComputor));
    //
    //        // Create handler for most active stock api
    //        server.createContext(
    //                "/mostactive", new MostActiveStockHandler(analyticsComputor));
    //
    //        // Create handler for average volume per second api
    //        server.createContext(
    //                "/averagevolume", new AverageVolumePerSecondHandler(analyticsComputor));

    // Start the server
    server.setExecutor(null); // Use the default executor
    server.start();

    Logger.info("Server is running on port 8000");
  }
}
