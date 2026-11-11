package com.neurolink.cosmicquest.ai;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Reads the AI settings from config.properties. The file is git-ignored, so
 * keys never reach GitHub; config.properties.example shows the settings.
 * A provider with no key is skipped, and a missing file simply means no AI.
 */
public final class AiConfig {

    /** The settings file, next to the project or the game. */
    public static final String FILE_NAME = "config.properties";

    static final String GEMINI_KEY = "gemini.api.key";
    static final String GEMINI_MODEL = "gemini.model";
    static final String GROQ_KEY = "groq.api.key";
    static final String GROQ_MODEL = "groq.model";
    static final String GROQ_URL = "groq.url";
    static final String TIMEOUT = "ai.timeout.seconds";

    private static final int DEFAULT_TIMEOUT_SECONDS = 15;
    private static final int MAX_TIMEOUT_SECONDS = 60;

    private final Properties settings;

    private AiConfig(Properties settings) {
        this.settings = settings;
    }

    /**
     * Reads the settings from a file. A missing or unreadable file gives
     * empty settings (no AI) instead of an error.
     *
     * @param file the config.properties file.
     * @return the settings.
     */
    public static AiConfig load(Path file) {
        Properties settings = new Properties();
        if (file != null && Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                settings.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                settings.clear();
            }
        }
        return new AiConfig(settings);
    }

    /**
     * Creates settings from ready-made values, for tests.
     *
     * @param settings the values.
     * @return the settings.
     */
    public static AiConfig of(Properties settings) {
        Properties copy = new Properties();
        if (settings != null) {
            copy.putAll(settings);
        }
        return new AiConfig(copy);
    }

    /**
     * Builds every provider that has a key, Gemini first, then Groq.
     *
     * @param transport sends the requests.
     * @return the providers; empty if no key is set.
     */
    public List<AiClient> buildClients(HttpTransport transport) {
        List<AiClient> clients = new ArrayList<>();
        Duration timeout = Duration.ofSeconds(timeoutSeconds());
        if (!value(GEMINI_KEY, "").isEmpty()) {
            clients.add(new GeminiClient(value(GEMINI_KEY, ""),
                    value(GEMINI_MODEL, GeminiClient.DEFAULT_MODEL), transport, timeout));
        }
        if (!value(GROQ_KEY, "").isEmpty()) {
            clients.add(new OpenAiCompatibleClient("Groq", value(GROQ_URL, OpenAiCompatibleClient.GROQ_URL),
                    value(GROQ_KEY, ""), value(GROQ_MODEL, OpenAiCompatibleClient.GROQ_DEFAULT_MODEL),
                    transport, timeout));
        }
        return clients;
    }

    /**
     * Builds the provider chain, or nothing if no key is set.
     *
     * @param transport sends the requests.
     * @return the chain, or empty for no AI.
     */
    public Optional<AiChain> buildChain(HttpTransport transport) {
        List<AiClient> clients = buildClients(transport);
        return clients.isEmpty() ? Optional.empty() : Optional.of(new AiChain(clients));
    }

    /** Returns the wait per request in seconds: 15 by default, 1 to 60. */
    public int timeoutSeconds() {
        try {
            int seconds = Integer.parseInt(value(TIMEOUT, String.valueOf(DEFAULT_TIMEOUT_SECONDS)));
            return Math.max(1, Math.min(seconds, MAX_TIMEOUT_SECONDS));
        } catch (NumberFormatException e) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
    }

    /** Returns a trimmed setting, or the default when it is missing or blank. */
    private String value(String key, String defaultValue) {
        String value = settings.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value.strip();
    }
}
