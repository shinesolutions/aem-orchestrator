package com.shinesolutions.aemorchestrator.util;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Component;

/**
 * Simple utilities class for performing common HTTP requests
 */
@Component
public class HttpUtil {
    
    @Resource
    private HttpClient httpClient;
    
    /**
     * Performs a HTTP GET request for a provided URL and returns the response code.
     * Normally used for performing health checks
     * @param url of the GET request
     * @return true if response is a HTTP status OK (200)
     * @throws IOException (normally if can't connect)
     * @throws ClientProtocolException if there's an error in the HTTP protocol
     */
    public boolean isHttpGetResponseOk(String url) throws ClientProtocolException, IOException {

        HttpGet request = new HttpGet(url);
        
        HttpResponse response = httpClient.execute(request);

        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }
}
