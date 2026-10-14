package com.shinesolutions.aemorchestrator;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stand-in for AEM Author behind its load balancer. The orchestrator only talks to Author over
 * HTTP: the startup health check and the swaggeraem4j replication/flush agent API. Every request is
 * recorded so tests can assert on what the real HTTP clients sent.
 */
final class FakeAem implements AutoCloseable {

  static final String HEALTH_CHECK_PATH = "/system/health";

  /** A recorded HTTP request; query string and form body parameters are merged. */
  record Request(
      String method, String path, String user, String password, Map<String, String> params) {

    String param(String name) {
      return params.get(name);
    }
  }

  private final HttpServer server;
  private final List<Request> requests = new CopyOnWriteArrayList<>();
  private volatile int healthCheckStatus = 200;

  FakeAem() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext("/", this::handle);
    server.start();
  }

  int port() {
    return server.getAddress().getPort();
  }

  void reset() {
    requests.clear();
    healthCheckStatus = 200;
  }

  void setHealthCheckStatus(int status) {
    healthCheckStatus = status;
  }

  List<Request> requests() {
    return List.copyOf(requests);
  }

  /** Agent API requests (create, pause, resume, delete) for the given agent name. */
  List<Request> agentRequests(String agentName) {
    return requests.stream()
        .filter(r -> r.method().equals("POST"))
        .filter(r -> r.path().startsWith("/etc/replication/agents."))
        .filter(r -> r.path().endsWith("/" + agentName))
        .toList();
  }

  @Override
  public void close() {
    server.stop(0);
  }

  private void handle(HttpExchange exchange) throws IOException {
    Map<String, String> params = new HashMap<>();
    parseParams(exchange.getRequestURI().getRawQuery(), params);
    parseParams(new String(exchange.getRequestBody().readAllBytes(), UTF_8), params);

    String[] credentials = basicAuth(exchange.getRequestHeaders().getFirst("Authorization"));
    requests.add(
        new Request(
            exchange.getRequestMethod(),
            exchange.getRequestURI().getPath(),
            credentials[0],
            credentials[1],
            params));

    int status =
        exchange.getRequestURI().getPath().equals(HEALTH_CHECK_PATH) ? healthCheckStatus : 200;
    byte[] body = "{}".getBytes(UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, body.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(body);
    }
  }

  private static void parseParams(String encoded, Map<String, String> params) {
    if (encoded == null || encoded.isEmpty()) {
      return;
    }
    for (String pair : encoded.split("&")) {
      int eq = pair.indexOf('=');
      String key = eq < 0 ? pair : pair.substring(0, eq);
      String value = eq < 0 ? "" : pair.substring(eq + 1);
      params.put(URLDecoder.decode(key, UTF_8), URLDecoder.decode(value, UTF_8));
    }
  }

  private static String[] basicAuth(String header) {
    if (header == null || !header.startsWith("Basic ")) {
      return new String[] {null, null};
    }
    String decoded = new String(Base64.getDecoder().decode(header.substring(6)), UTF_8);
    int colon = decoded.indexOf(':');
    return new String[] {decoded.substring(0, colon), decoded.substring(colon + 1)};
  }

  /** Paths of all recorded requests, for assertion messages. */
  List<String> describe() {
    List<String> out = new ArrayList<>();
    requests.forEach(r -> out.add(r.method() + " " + r.path() + " " + r.params()));
    return out;
  }
}
