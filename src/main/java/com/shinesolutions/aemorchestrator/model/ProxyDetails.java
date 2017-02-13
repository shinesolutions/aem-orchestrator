package com.shinesolutions.aemorchestrator.model;

public class ProxyDetails {

    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    public ProxyDetails withHost(String host) {
        this.setHost(host);
        return this;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public ProxyDetails withPort(int port) {
        this.setPort(port);
        return this;
    }
}
