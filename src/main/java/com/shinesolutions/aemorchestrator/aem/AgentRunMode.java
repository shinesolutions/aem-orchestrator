package com.shinesolutions.aemorchestrator.aem;

/**
 * Run mode used to locate an agent under {@code /etc/replication/agents.<runMode>}.
 *
 * <p>All constants resolve to {@code author} because every agent the orchestrator manages,
 * including those for publish and preview publish instances, lives on the author instance.
 */
public enum AgentRunMode {
  AUTHOR("author"),
  PUBLISH("author"),
  PREVIEWPUBLISH("author");

  private final String value;

  private AgentRunMode(String v) {
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
