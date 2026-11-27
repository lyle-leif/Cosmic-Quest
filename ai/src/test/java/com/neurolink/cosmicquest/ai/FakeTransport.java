package com.neurolink.cosmicquest.ai;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * A transport that never touches the network: it returns a ready-made reply
 * (or throws a ready-made error) and remembers the last request.
 */
class FakeTransport implements HttpTransport {

    private final Reply reply;
    private final IOException error;
    String lastUrl;
    Map<String, String> lastHeaders;
    String lastBody;
    Duration lastTimeout;
    int calls;

    private FakeTransport(Reply reply, IOException error) {
        this.reply = reply;
        this.error = error;
    }

    static FakeTransport replying(int status, String body) {
        return new FakeTransport(new Reply(status, body), null);
    }

    static FakeTransport failing(IOException error) {
        return new FakeTransport(null, error);
    }

    @Override
    public Reply post(String url, Map<String, String> headers, String body, Duration timeout) throws IOException {
        calls++;
        lastUrl = url;
        lastHeaders = headers;
        lastBody = body;
        lastTimeout = timeout;
        if (error != null) {
            throw error;
        }
        return reply;
    }
}
