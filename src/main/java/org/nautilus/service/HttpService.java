package org.nautilus.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpService {
    private String edoServiceUrl;
    private String authHeader;

    public HttpService(String edoServiceUrl, String authHeader) {
        this.edoServiceUrl = edoServiceUrl;
        this.authHeader = authHeader;
    }
    public HttpResponse executeGet(String path) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(edoServiceUrl + path);
            httpGet.setHeader("Authorization", authHeader);
            return httpClient.execute(httpGet);
        }
    }

    public HttpResponse executePost(String path, String body) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(edoServiceUrl + path);
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            httpPost.setHeader("Authorization", authHeader);
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            return httpClient.execute(httpPost);
        }
    }
}
