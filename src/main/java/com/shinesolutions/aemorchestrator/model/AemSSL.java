package com.shinesolutions.aemorchestrator.model;

public enum AemSSL {
    RELAXED("relaxed"),
    CLIENTAUTH("clientauth"),
    DEFAULT("default");
    
    private final String value;

    private AemSSL(String v) {
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
