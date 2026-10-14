package com.shinesolutions.aemorchestrator.config;

import com.shinesolutions.aemorchestrator.model.ProxyDetails;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that detects an outbound proxy from the {@code https_proxy} environment
 * variable.
 */
@Configuration
public class ProxyConfig {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private static final String ENV_HTTPS_PROXY = "https_proxy";

  /**
   * Provides the proxy host and port parsed from the {@code https_proxy} environment variable.
   *
   * <p>When the variable is unset, empty or not a valid URL, the host is empty so that no proxy is
   * detected. When the URL has no port, the port is -1.
   */
  @Bean
  public ProxyDetails proxyDetails() {

    ProxyDetails details = new ProxyDetails().withHost("");
    String httpsProxyEnvironmentVar = System.getenv(ENV_HTTPS_PROXY);

    if (httpsProxyEnvironmentVar != null && !httpsProxyEnvironmentVar.isEmpty()) {
      logger.debug(ENV_HTTPS_PROXY + " environment variable detected: " + httpsProxyEnvironmentVar);
      try {
        URL httpProxyUrl = new URI(httpsProxyEnvironmentVar).toURL();
        details =
            new ProxyDetails().withHost(httpProxyUrl.getHost()).withPort(httpProxyUrl.getPort());
        logger.debug(
            "Proxy details set to host: " + details.getHost() + ", port: " + details.getPort());
      } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
        logger.warn("Unable to parse " + ENV_HTTPS_PROXY + " environment variable", e);
      }
    } else {
      logger.debug(ENV_HTTPS_PROXY + " environment variable not found, no proxy details set");
    }

    return details;
  }
}
