package edu.bu.web;

import edu.bu.shadow.DeviceShadow;
import edu.bu.shadow.ShadowService;
import edu.bu.shadow.ShadowUpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for reading and updating device shadow state. */
@RestController
@RequestMapping("/shadow")
public class ShadowController {

  private static final Logger log = LoggerFactory.getLogger(ShadowController.class);

  private final ShadowService shadowService;

  public ShadowController(ShadowService shadowService) {
    this.shadowService = shadowService;
  }

  /** Returns the current shadow state for a buoy, or 404 if no shadow has been received yet. */
  @GetMapping("/{buoyId}")
  public ResponseEntity<DeviceShadow> getShadow(@PathVariable String buoyId) {
    return shadowService
        .getShadow(buoyId)
        .map(shadow -> {
          log.info("Handled shadow GET for buoy {}", buoyId);
          return ResponseEntity.ok(shadow);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  /** Publishes a desired state update to the AWS IoT Core shadow for the given buoy. */
  @PostMapping("/{buoyId}/desired")
  public ResponseEntity<Void> updateDesired(
      @PathVariable String buoyId, @RequestBody ShadowUpdateMessage.ShadowFields desired) {
    shadowService.publishDesiredState(buoyId, desired);
    log.info("Handled shadow POST desired for buoy {}", buoyId);
    return ResponseEntity.accepted().build();
  }
}
