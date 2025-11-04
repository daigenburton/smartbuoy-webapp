package edu.bu;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.tinylog.Logger;

/**
 * Entry point for the entire single process SmartBuoy. It initializes the following collaborators:
 * <li>BasicWebServer - to configure supported HTTP endpoints for our (SmartBuoy) users
 * <li>DataStore - an instance of a data store to store data
 */
public class SmartBuoyServer {

  // StockAppServer
  public static void main(String[] args) throws IOException, URISyntaxException {
    Logger.info("Starting SmartBuoyServer with arguments: {}", List.of(args));
  }
}
