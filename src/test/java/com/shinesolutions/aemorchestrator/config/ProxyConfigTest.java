package com.shinesolutions.aemorchestrator.config;

import com.shinesolutions.aemorchestrator.model.ProxyDetails;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ProxyConfigTest {
    
    private ProxyConfig proxyConfig;
    
    @Before
    public void setup() {
        proxyConfig = new ProxyConfig();
    }
    
    @Test
    public void testProxyDetails_NoEnvironmentVariable() {
        ProxyDetails details = proxyConfig.proxyDetails();

        String httpProxyHost = "";

        assertThat(details.getHost(), equalTo(httpProxyHost));
    }
}
