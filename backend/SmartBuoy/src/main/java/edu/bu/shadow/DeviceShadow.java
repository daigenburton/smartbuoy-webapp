package edu.bu.shadow;

import java.time.Instant;

public record DeviceShadow(
    String buoyId,
    Integer battery,
    String status,
    Integer sampleIntervalSec,
    Integer reportedSampleIntervalSec,
    Instant lastUpdated) {}
