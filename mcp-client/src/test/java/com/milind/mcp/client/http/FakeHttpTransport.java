package com.milind.mcp.client.http;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Test double for {@link HttpTransport}: returns a queue of canned response bodies (one
 * per call, in order) and records every request made, so tests can assert on both what
 * was sent and drive what comes back - without any real network access.
 */
public class FakeHttpTransport implements HttpTransport {

    public record RecordedRequest(String url, String jsonBody, Map<String, String> headers) {
    }

    private final Deque<String> responses = new ArrayDeque<>();
    private final List<RecordedRequest> requests = new ArrayList<>();

    public FakeHttpTransport queueResponse(String responseBody) {
        responses.addLast(responseBody);
        return this;
    }

    @Override
    public String postJson(String url, String jsonBody, Map<String, String> headers) {
        requests.add(new RecordedRequest(url, jsonBody, headers));
        if (responses.isEmpty()) {
            throw new IllegalStateException("FakeHttpTransport has no more queued responses for: " + url);
        }
        return responses.removeFirst();
    }

    public List<RecordedRequest> getRequests() {
        return requests;
    }

    public RecordedRequest lastRequest() {
        return requests.get(requests.size() - 1);
    }
}
