package com.shinesolutions.aemorchestrator.model;

/** Host and port of the outbound proxy for AWS clients; an empty host means no proxy. */
public class ProxyDetails {

  private String host;
  private int port;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  /** Sets the host and returns this object for chaining. */
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

  /** Sets the port and returns this object for chaining. */
  public ProxyDetails withPort(int port) {
    this.setPort(port);
    return this;
  }
}
