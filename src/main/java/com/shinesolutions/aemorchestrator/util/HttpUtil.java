package com.shinesolutions.aemorchestrator.util;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Simple utilities class for performing common HTTP requests
 */
@Component
public class HttpUtil {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${http.client.relaxed.ssl.enable}")
    private boolean enableRelaxedSslHttpClient;

    /**
     * Performs a HTTP GET request for a provided URL and returns the response code.
     * Normally used for performing health checks
     *
     * @param url of the GET request
     * @return true if response is a HTTP status OK (200)
     * @throws IOException (normally if can't connect)
     * @throws ClientProtocolException if there's an error in the HTTP protocol
     */
    public boolean isHttpGetResponseOk(String url) throws ClientProtocolException, IOException {

        logger.debug("Use relaxed SSL HTTP Client settings: " + enableRelaxedSslHttpClient);

        int statusCode;

        try (CloseableHttpClient client = buildCloseableHttpClient()) {

            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = client.execute(request)) {
                statusCode = response.getStatusLine().getStatusCode();
            }

        }

        return statusCode == HttpStatus.SC_OK;
    }

    private CloseableHttpClient buildCloseableHttpClient() {

        CloseableHttpClient client;

        if (enableRelaxedSslHttpClient) {
            client = HttpClientBuilder.create()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();

        } else {
            client = HttpClientBuilder.create().build();
        }

        return client;
    }
}
