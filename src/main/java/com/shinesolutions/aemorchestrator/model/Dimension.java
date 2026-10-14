package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A CloudWatch metric dimension, its {@code name} and {@code value}, from the {@code Dimensions} of
 * an alarm {@link Trigger}.
 */
public class Dimension {
  @JsonProperty("name")
  private String name;

  @JsonProperty("value")
  private String value;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
