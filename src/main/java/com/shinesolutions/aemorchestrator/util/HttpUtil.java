package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

/**
 * Simple utilities class for performing common HTTP requests
 */
@Component
public class HttpUtil {
    
    /**
     * Performs a HTTP GET request for a provided URL and returns the response code.
     * Normally used for performing health checks
     * @param url
     * @return HTTP status code
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public int getHttpResponseCode(String url) throws ClientProtocolException, IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        
        HttpResponse response = client.execute(request);

        return response.getStatusLine().getStatusCode();
    }
}
