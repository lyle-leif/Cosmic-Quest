package com.neurolink.cosmicquest.ai;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * Sends one HTTP POST. The real version uses Java's {@code HttpClient}; tests
 * pass a fake that returns ready-made replies, so no test uses the network.
 */
public interface HttpTransport {

    /**
     * A reply: the status code and the body text.
     *
     * @param status the HTTP status, such as 200 or 429.
     * @param body the reply text.
     */
    record Reply(int status, String body) {
    }

    /**
     * Sends a JSON POST request.
     *
     * @param url where to send it.
     * @param headers extra headers, such as the API key.
     * @param body the JSON body.
     * @param timeout how long to wait before giving up.
     * @return the reply.
     * @throws IOException if there is no connection or the time runs out.
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    Reply post(String url, Map<String, String> headers, String body, Duration timeout)
            throws IOException, InterruptedException;
}
