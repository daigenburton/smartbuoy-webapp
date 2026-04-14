package edu.bu.shadow;

import java.time.Instant;

public record DeviceShadow(
    String buoyId,
    Integer battery,
    Boolean led,
    Boolean buzzer,
    Boolean deployed,
    Instant lastUpdated) {}
