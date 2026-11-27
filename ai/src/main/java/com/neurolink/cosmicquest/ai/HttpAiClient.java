package com.neurolink.cosmicquest.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * The shared steps of every online provider: build the prompt, send it, check
 * the status, read the model's text and parse the questions. Each provider
 * only fills in its own address, headers, request body and reply format
 * (template method).
 */
public abstract class HttpAiClient implements AiClient {

    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final String name;
    private final HttpTransport transport;
    private final Duration timeout;

    /**
     * Prepares a provider.
     *
     * @param name a short name for messages.
     * @param transport sends the request.
     * @param timeout how long to wait for a reply.
     * @throws IllegalArgumentException if an argument is null.
     */
    protected HttpAiClient(String name, HttpTransport transport, Duration timeout) {
        if (name == null || transport == null || timeout == null) {
            throw new IllegalArgumentException("Name, transport and timeout must not be null.");
        }
        this.name = name;
        this.transport = transport;
        this.timeout = timeout;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<AiQuestion> generate(AiRequest request) throws AiException {
        if (request == null) {
            throw new IllegalArgumentException("The request must not be null.");
        }
        HttpTransport.Reply reply = send(requestBody(PromptBuilder.build(request)));
        if (reply.status() != HTTP_OK) {
            throw new AiException(name + " returned HTTP " + reply.status() + hint(reply.status()) + ".");
        }
        JsonObject json;
        try {
            json = JsonParser.parseString(reply.body()).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            throw new AiException(name + " sent a reply that is not JSON.", e);
        }
        List<AiQuestion> questions = AiQuestionParser.parse(modelText(json), request.getKind(), request.getCount());
        if (questions.isEmpty()) {
            throw new AiException(name + " wrote no questions.");
        }
        return questions;
    }

    private HttpTransport.Reply send(String body) throws AiException {
        try {
            return transport.post(url(), headers(), body, timeout);
        } catch (HttpTimeoutException e) {
            throw new AiException(name + " took too long to answer.", e);
        } catch (IOException e) {
            throw new AiException(name + " could not be reached.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException(name + " was interrupted.", e);
        } catch (RuntimeException e) {
            throw new AiException(name + " could not send the request.", e);
        }
    }

    /** A short hint for the player about what an error status usually means. */
    private static String hint(int status) {
        switch (status) {
            case HTTP_TOO_MANY_REQUESTS:
                return " (rate limit reached)";
            case HTTP_BAD_REQUEST:
            case HTTP_UNAUTHORIZED:
            case HTTP_FORBIDDEN:
                return " (check the key in config.properties)";
            case HTTP_NOT_FOUND:
                return " (check the model name in config.properties)";
            default:
                return "";
        }
    }

    /** Returns the address to send the request to. */
    protected abstract String url();

    /** Returns the headers, including the key. */
    protected abstract Map<String, String> headers();

    /**
     * Builds this provider's JSON request body.
     *
     * @param prompt the instructions and lesson.
     * @return the request body.
     */
    protected abstract String requestBody(String prompt);

    /**
     * Takes the text the model wrote out of this provider's reply.
     *
     * @param reply the reply as JSON.
     * @return the model's text.
     * @throws AiException if the reply holds no text.
     */
    protected abstract String modelText(JsonObject reply) throws AiException;

    /**
     * Checks a key or other required setting.
     *
     * @param value the setting.
     * @param what its name, for the message.
     * @return the setting, trimmed.
     * @throws IllegalArgumentException if it is null or blank.
     */
    protected static String required(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " must not be blank.");
        }
        return value.strip();
    }
}
