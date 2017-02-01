package com.shinesolutions.aemorchestrator.aem;

public enum AgentRunMode {
    AUTHOR("author"),
    PUBLISH("author");
    
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
