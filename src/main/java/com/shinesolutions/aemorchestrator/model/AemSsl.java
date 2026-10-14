package com.shinesolutions.aemorchestrator.model;

/**
 * SSL modes for the AEM replication and flush agents the orchestrator creates, each holding the
 * value set as the agent's {@code ssl} property.
 */
public enum AemSsl {
  RELAXED("relaxed"),
  CLIENTAUTH("clientauth"),
  DEFAULT("default");

  private final String value;

  private AemSsl(String v) {
    value = v;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public String getValue() {
    return this.value;
  }
}
