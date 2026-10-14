package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A CloudWatch metric dimension, its {@code name} and {@code value}, from the {@code Dimensions} of
 * an alarm {@link Trigger}.
 */
public final class Dimension {

  private final String name;
  private final String value;

  /** Creates a dimension; also used by Jackson to map the alarm message JSON. */
  @JsonCreator
  public Dimension(@JsonProperty("name") String name, @JsonProperty("value") String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
