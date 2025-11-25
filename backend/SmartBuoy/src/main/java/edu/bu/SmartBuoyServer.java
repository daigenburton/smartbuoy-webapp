package edu.bu;

import edu.bu.data.DataStore;
import edu.bu.data.InMemoryStore;
import edu.bu.server.BasicWebServer;
import edu.bu.sqs.SQSQueueReader;
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

    // set up store
    DataStore store = new InMemoryStore();

    // mock data-
    // DataStore store = edu.bu.mock.MockDataGenerator.generate();

    /*
    // Load database configuration
    DatabaseConfig dbConfig = new DatabaseConfig("database.properties");
    // Create database store
    DataStore store =
        new InDatabaseStore(dbConfig.getJdbcUrl(), dbConfig.getUsername(), dbConfig.getPassword());
    */

    // start web server
    BasicWebServer webServer = new BasicWebServer(store);
    webServer.start();

    // start SQSQueueReader service
    SQSQueueReader queueReader = new SQSQueueReader(store);
    queueReader.start();
  }
}
