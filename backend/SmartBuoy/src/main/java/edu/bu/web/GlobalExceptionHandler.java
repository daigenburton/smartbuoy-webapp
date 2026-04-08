package edu.bu.web;

import edu.bu.analytics.UnknownBuoyException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Centralized exceptionception handler that maps domain exceptionceptions to HTTP responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Maps UnknownBuoyException to HTTP 404 Not Found. */
  @ExceptionHandler(UnknownBuoyException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, String> handleUnknownBuoy(UnknownBuoyException exception) {
    return Map.of("error", exception.getMessage());
  }

  /** Maps IllegalArgumentException to HTTP 400 Bad Request. */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleIllegalArgument(IllegalArgumentException exception) {
    return Map.of("error", exception.getMessage());
  }
}
