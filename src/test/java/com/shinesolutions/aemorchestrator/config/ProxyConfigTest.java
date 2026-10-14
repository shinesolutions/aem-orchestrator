package com.shinesolutions.aemorchestrator.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.shinesolutions.aemorchestrator.model.ProxyDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProxyConfigTest {

  private ProxyConfig proxyConfig;

  @BeforeEach
  void setup() {
    proxyConfig = new ProxyConfig();
  }

  @Test
  void testProxyDetails_noEnvironmentVariable() {
    ProxyDetails details = proxyConfig.proxyDetails();

    String httpProxyHost = "";

    assertThat(details.getHost(), equalTo(httpProxyHost));
  }
}
