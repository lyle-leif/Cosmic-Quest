package com.neurolink.cosmicquest.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.Map;

/**
 * Any provider that accepts OpenAI-style chat completion requests, such as
 * Groq or OpenRouter. Only the address, model and key change between them,
 * and all three come from config.properties.
 */
public class OpenAiCompatibleClient extends HttpAiClient {

    /** Groq's chat completions address, the default second provider. */
    public static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    /** The Groq model used when config.properties names none. */
    public static final String GROQ_DEFAULT_MODEL = "openai/gpt-oss-20b";

    private static final double TEMPERATURE = 0.7;
    private static final String SYSTEM_MESSAGE =
            "You write quiz questions as JSON only. Never add text outside the JSON.";

    private final String url;
    private final String apiKey;
    private final String model;

    /**
     * Prepares an OpenAI-compatible provider.
     *
     * @param name a short name for messages, such as "Groq".
     * @param url the chat completions address.
     * @param apiKey the provider's key, sent as a Bearer token.
     * @param model the model name.
     * @param transport sends the request.
     * @param timeout how long to wait.
     * @throws IllegalArgumentException if the address, key or model is blank, or an argument is null.
     */
    public OpenAiCompatibleClient(String name, String url, String apiKey, String model,
            HttpTransport transport, Duration timeout) {
        super(name, transport, timeout);
        this.url = required(url, "The " + name + " address");
        this.apiKey = required(apiKey, "The " + name + " API key");
        this.model = required(model, "The " + name + " model");
    }

    @Override
    protected String url() {
        return url;
    }

    @Override
    protected Map<String, String> headers() {
        return Map.of("Authorization", "Bearer " + apiKey);
    }

    @Override
    protected String requestBody(String prompt) {
        JsonArray messages = new JsonArray();
        messages.add(message("system", SYSTEM_MESSAGE));
        messages.add(message("user", prompt));
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messages);
        body.addProperty("temperature", TEMPERATURE);
        return body.toString();
    }

    private static JsonObject message(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    /** Reads choices[0].message.content. */
    @Override
    protected String modelText(JsonObject reply) throws AiException {
        try {
            return reply.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
        } catch (RuntimeException e) {
            throw new AiException(getName() + " sent no text.", e);
        }
    }
}
