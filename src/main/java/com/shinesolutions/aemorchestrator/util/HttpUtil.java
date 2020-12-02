package com.shinesolutions.aemorchestrator.util;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.conn.ssl.SSLContextBuilder;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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
    public boolean isHttpGetResponseOk(String url) throws ClientProtocolException, IOException, KeyStoreException, KeyManagementException, NoSuchAlgorithmException {

        int statusCode;

        try (CloseableHttpClient client = buildCloseableHttpClient()) {

            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = client.execute(request)) {
                statusCode = response.getStatusLine().getStatusCode();
            }

        }

        return statusCode == HttpStatus.SC_OK;
    }

    private CloseableHttpClient buildCloseableHttpClient() throws KeyStoreException, KeyManagementException, NoSuchAlgorithmException{

        CloseableHttpClient client;

        if (enableRelaxedSslHttpClient) {
            
            // Need to also trust self-signed certificates besides CA signed ones
            SSLContext sslContext = new SSLContextBuilder()
              .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
              .build();

            client = HttpClientBuilder.create()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

        } else {
            client = HttpClientBuilder.create().build();
        }

        return client;
    }
}
