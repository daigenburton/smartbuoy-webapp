package edu.bu.shadow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShadowUpdateMessage {

  private ShadowState state;

  public ShadowState getState() {
    return state;
  }

  public void setState(ShadowState state) {
    this.state = state;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ShadowState {

    private ShadowFields desired;
    private ShadowFields reported;
    private ShadowFields delta;

    public ShadowFields getDesired() {
      return desired;
    }

    public void setDesired(ShadowFields desired) {
      this.desired = desired;
    }

    public ShadowFields getReported() {
      return reported;
    }

    public void setReported(ShadowFields reported) {
      this.reported = reported;
    }

    public ShadowFields getDelta() {
      return delta;
    }

    public void setDelta(ShadowFields delta) {
      this.delta = delta;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ShadowFields {

    private Integer battery;
    private String status;
    private Integer sampleIntervalSec;

    public Integer getBattery() {
      return battery;
    }

    public void setBattery(Integer battery) {
      this.battery = battery;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Integer getSampleIntervalSec() {
      return sampleIntervalSec;
    }

    public void setSampleIntervalSec(Integer sampleIntervalSec) {
      this.sampleIntervalSec = sampleIntervalSec;
    }
  }
}
