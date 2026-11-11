package com.neurolink.cosmicquest.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AiConfigTest {

    @TempDir
    Path folder;

    private final FakeTransport transport = FakeTransport.replying(200, "{}");

    private static AiConfig config(String... keyValues) {
        Properties settings = new Properties();
        for (int i = 0; i < keyValues.length; i += 2) {
            settings.setProperty(keyValues[i], keyValues[i + 1]);
        }
        return AiConfig.of(settings);
    }

    @Test
    void missingFileMeansNoAi() {
        AiConfig config = AiConfig.load(folder.resolve("config.properties"));
        assertTrue(config.buildClients(transport).isEmpty());
        assertTrue(config.buildChain(transport).isEmpty());
    }

    @Test
    void blankKeysAreSkipped() {
        assertTrue(config(AiConfig.GEMINI_KEY, "  ", AiConfig.GROQ_KEY, "").buildClients(transport).isEmpty());
    }

    @Test
    void geminiComesBeforeGroq() {
        List<AiClient> clients = config(AiConfig.GROQ_KEY, "g", AiConfig.GEMINI_KEY, "k").buildClients(transport);
        assertEquals(2, clients.size());
        assertInstanceOf(GeminiClient.class, clients.get(0));
        assertEquals("Groq", clients.get(1).getName());
    }

    @Test
    void onlyGroqKeyGivesOnlyGroq() {
        List<AiClient> clients = config(AiConfig.GROQ_KEY, "g").buildClients(transport);
        assertEquals(1, clients.size());
        assertEquals("Groq", clients.get(0).getName());
    }

    @Test
    void readsKeysAndModelsFromAFile() throws IOException, AiException {
        Path file = folder.resolve("config.properties");
        Files.writeString(file, "# test\ngemini.api.key = abc \ngemini.model=my-model\nai.timeout.seconds=5\n");
        AiConfig config = AiConfig.load(file);
        assertEquals(5, config.timeoutSeconds());
        FakeTransport replying = FakeTransport.replying(200, AiTestData.geminiReply(AiTestData.QUESTIONS_JSON));
        config.buildChain(replying).orElseThrow().generate(AiTestData.request(AiQuestionKind.IDENTIFICATION, 1));
        assertEquals("abc", replying.lastHeaders.get("x-goog-api-key"), "Values are trimmed");
        assertTrue(replying.lastUrl.contains("/models/my-model:"));
    }

    @Test
    void badTimeoutFallsBackToTheDefault() {
        assertEquals(15, config().timeoutSeconds());
        assertEquals(15, config(AiConfig.TIMEOUT, "soon").timeoutSeconds());
        assertEquals(60, config(AiConfig.TIMEOUT, "999").timeoutSeconds());
        assertEquals(1, config(AiConfig.TIMEOUT, "-3").timeoutSeconds());
    }

    @Test
    void exampleFileHasNoKeys() throws IOException {
        Path example = Path.of("..", "config.properties.example");
        assertTrue(Files.isRegularFile(example), "config.properties.example must be committed");
        Properties settings = new Properties();
        try (var reader = Files.newBufferedReader(example)) {
            settings.load(reader);
        }
        assertEquals("", settings.getProperty(AiConfig.GEMINI_KEY), "The example must never hold a real key");
        assertEquals("", settings.getProperty(AiConfig.GROQ_KEY), "The example must never hold a real key");
    }
}
