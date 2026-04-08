package edu.bu.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Simple liveness endpoint so the frontend can verify backend connectivity. */
@RestController
public class HealthController {

  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }
}
