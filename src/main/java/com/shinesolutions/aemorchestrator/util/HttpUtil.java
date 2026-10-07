package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Simple utilities class for performing common HTTP requests */
@Component
public class HttpUtil {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${http.client.relaxed.ssl.enable}")
  private boolean enableRelaxedSslHttpClient;

  /**
   * Performs a HTTP GET request for a provided URL and returns the response code. Normally used for
   * performing health checks
   *
   * @param url of the GET request
   * @return true if response is a HTTP status OK (200)
   * @throws IOException (normally if can't connect)
   * @throws ClientProtocolException if there's an error in the HTTP protocol
   * @throws KeyStoreException if there's an error with the SSL Keystore
   * @throws KeyManagementException if there's an error with the SSL Keymanagement
   * @throws NoSuchAlgorithmException if there's an error with the SSL Algorithm
   */
  public boolean isHttpGetResponseOk(String url)
      throws ClientProtocolException,
          IOException,
          KeyStoreException,
          KeyManagementException,
          NoSuchAlgorithmException {

    int statusCode;

    try (CloseableHttpClient client = buildCloseableHttpClient()) {

      HttpGet request = new HttpGet(url);

      try (CloseableHttpResponse response = client.execute(request)) {
        statusCode = response.getCode();
      }
    }

    return statusCode == HttpStatus.SC_OK;
  }

  private CloseableHttpClient buildCloseableHttpClient()
      throws KeyStoreException, KeyManagementException, NoSuchAlgorithmException {

    CloseableHttpClient client;

    if (enableRelaxedSslHttpClient) {

      // Need to also trust self-signed certificates besides CA signed ones
      SSLContext sslContext =
          SSLContextBuilder.create().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();

      SSLConnectionSocketFactory sslSocketFactory =
          SSLConnectionSocketFactoryBuilder.create()
              .setSslContext(sslContext)
              .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
              .build();

      HttpClientConnectionManager connectionManager =
          PoolingHttpClientConnectionManagerBuilder.create()
              .setSSLSocketFactory(sslSocketFactory)
              .build();

      client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

    } else {
      client = HttpClientBuilder.create().build();
    }

    return client;
  }
}
