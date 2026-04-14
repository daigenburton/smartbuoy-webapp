package edu.bu.web;

import edu.bu.shadow.DeviceShadow;
import edu.bu.shadow.ShadowService;
import edu.bu.shadow.ShadowUpdateMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shadow")
public class ShadowController {

  @Value("${mqtt.thing-name:esp32}")
  private String thingName;

  private final ShadowService shadowService;

  public ShadowController(ShadowService shadowService) {
    this.shadowService = shadowService;
  }

  @GetMapping("/{buoyId}")
  public ResponseEntity<DeviceShadow> getShadow(@PathVariable String buoyId) {
    return shadowService
        .getShadow(thingName)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{buoyId}")
  public ResponseEntity<Void> updateDesired(
      @PathVariable String buoyId, @RequestBody ShadowUpdateMessage.ShadowFields desired) {
    shadowService.publishDesiredState(thingName, desired);
    return ResponseEntity.accepted().build();
  }
}
