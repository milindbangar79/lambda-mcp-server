package com.milind.mcp.client.http;

import java.util.Map;

/**
 * The one seam between this client's HTTP-speaking classes ({@code McpClient},
 * {@code AnthropicClient}) and the network. Both the router's API Gateway endpoint and
 * the Anthropic Messages API are just "POST JSON, get JSON back" over HTTPS, so a single
 * narrow interface covers both - and lets tests supply canned responses instead of
 * making real network calls.
 */
public interface HttpTransport {

    /**
     * POSTs {@code jsonBody} to {@code url} with the given headers (plus
     * {@code Content-Type: application/json}, always added) and returns the response body.
     *
     * @throws HttpTransportException on a non-2xx response or a network-level failure
     */
    String postJson(String url, String jsonBody, Map<String, String> headers);
}
