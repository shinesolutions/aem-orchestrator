package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.stereotype.Component;

/**
 * Simple utilities class for performing common HTTP requests
 */
@Component
public class HttpUtil {
    
    /**
     * Performs a HTTP GET request for a provided URL and returns the response code.
     * Normally used for performing health checks
     * @param url of the GET request
     * @return HTTP status code (integer form)
     * @throws IOException (normally if can't connect)
     * @throws ClientProtocolException if there's an error in the HTTP protocol
     * @throws KeyStoreException if there is an error with the key store
     * @throws NoSuchAlgorithmException if a cryptographic algorithm is request, but not supported
     * @throws KeyManagementException if any issues with key management
     */
    public boolean isHttpGetResponseOk(String url) throws ClientProtocolException, IOException, 
        KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        
        SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (certificate, authType) -> true).build();
       
          CloseableHttpClient client = HttpClients.custom()
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .build();
        
        HttpGet request = new HttpGet(url);
        
        HttpResponse response = client.execute(request);

        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }
}
