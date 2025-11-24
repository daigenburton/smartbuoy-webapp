package edu.bu;

import java.io.IOException;

/** Defines the interface for the Finnhub response handlers */
public interface ResponseHandler {

  void enqueue(String message) throws IOException, InterruptedException;
}
