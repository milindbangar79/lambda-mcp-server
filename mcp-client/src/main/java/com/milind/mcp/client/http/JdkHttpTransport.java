package com.milind.mcp.client.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * The real {@link HttpTransport}, backed by the JDK's built-in {@code java.net.http}
 * client - no HTTP library dependency needed for a CLI that makes a handful of requests
 * per run.
 */
public class JdkHttpTransport implements HttpTransport {

    private final HttpClient httpClient;
    private final Duration requestTimeout;

    public JdkHttpTransport() {
        this(Duration.ofSeconds(60));
    }

    public JdkHttpTransport(Duration requestTimeout) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.requestTimeout = requestTimeout;
    }

    @Override
    public String postJson(String url, String jsonBody, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(requestTimeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));

        headers.forEach(requestBuilder::header);

        HttpResponse<String> response;
        try {
            response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new HttpTransportException("Request to " + url + " failed: " + e.getMessage(), e);
        }

        if (response.statusCode() / 100 != 2) {
            throw new HttpTransportException(
                    "POST " + url + " returned HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }
}
