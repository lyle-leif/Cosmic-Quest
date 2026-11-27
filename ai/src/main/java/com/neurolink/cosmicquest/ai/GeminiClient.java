package com.neurolink.cosmicquest.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.Map;

/**
 * Google Gemini, through the generateContent endpoint. The key is sent in the
 * {@code x-goog-api-key} header, never in the address.
 */
public class GeminiClient extends HttpAiClient {

    /** The model used when config.properties names none. */
    public static final String DEFAULT_MODEL = "gemini-3.5-flash-lite";

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final double TEMPERATURE = 0.7;

    private final String apiKey;
    private final String model;

    /**
     * Prepares the Gemini provider.
     *
     * @param apiKey the key from Google AI Studio.
     * @param model the model, such as {@link #DEFAULT_MODEL}.
     * @param transport sends the request.
     * @param timeout how long to wait.
     * @throws IllegalArgumentException if the key or model is blank, or an argument is null.
     */
    public GeminiClient(String apiKey, String model, HttpTransport transport, Duration timeout) {
        super("Gemini", transport, timeout);
        this.apiKey = required(apiKey, "The Gemini API key");
        this.model = required(model, "The Gemini model");
    }

    @Override
    protected String url() {
        return BASE_URL + model + ":generateContent";
    }

    @Override
    protected Map<String, String> headers() {
        return Map.of("x-goog-api-key", apiKey);
    }

    @Override
    protected String requestBody(String prompt) {
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.addProperty("role", "user");
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject config = new JsonObject();
        config.addProperty("responseMimeType", "application/json");
        config.addProperty("temperature", TEMPERATURE);

        JsonObject body = new JsonObject();
        body.add("contents", contents);
        body.add("generationConfig", config);
        return body.toString();
    }

    /** Joins the text parts of the first candidate. */
    @Override
    protected String modelText(JsonObject reply) throws AiException {
        try {
            JsonArray parts = reply.getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content").getAsJsonArray("parts");
            StringBuilder text = new StringBuilder();
            for (JsonElement part : parts) {
                JsonElement value = part.getAsJsonObject().get("text");
                if (value != null && value.isJsonPrimitive()) {
                    text.append(value.getAsString());
                }
            }
            return text.toString();
        } catch (RuntimeException e) {
            throw new AiException("Gemini sent no text.", e);
        }
    }
}
