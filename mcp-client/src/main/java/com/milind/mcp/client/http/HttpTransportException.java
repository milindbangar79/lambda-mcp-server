package com.milind.mcp.client.http;

/** Raised by {@link HttpTransport} on a non-2xx HTTP response or a network-level failure. */
public class HttpTransportException extends RuntimeException {

    public HttpTransportException(String message) {
        super(message);
    }

    public HttpTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
