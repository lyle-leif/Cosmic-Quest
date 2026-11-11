package com.neurolink.cosmicquest.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Gemini and the OpenAI-compatible provider, tested against fake replies only. */
class HttpAiClientTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final AiRequest REQUEST = AiTestData.request(AiQuestionKind.IDENTIFICATION, 2);

    private static GeminiClient gemini(FakeTransport transport) {
        return new GeminiClient("test-key", GeminiClient.DEFAULT_MODEL, transport, TIMEOUT);
    }

    private static OpenAiCompatibleClient groq(FakeTransport transport) {
        return new OpenAiCompatibleClient("Groq", OpenAiCompatibleClient.GROQ_URL, "test-key",
                OpenAiCompatibleClient.GROQ_DEFAULT_MODEL, transport, TIMEOUT);
    }

    @Test
    void geminiSendsTheKeyInAHeaderAndReadsTheQuestions() throws AiException {
        FakeTransport transport = FakeTransport.replying(200, AiTestData.geminiReply(AiTestData.QUESTIONS_JSON));
        List<AiQuestion> questions = gemini(transport).generate(REQUEST);

        assertEquals(2, questions.size());
        assertEquals("Class", questions.get(0).getAnswer());
        assertTrue(transport.lastUrl.endsWith("/models/" + GeminiClient.DEFAULT_MODEL + ":generateContent"));
        assertFalse(transport.lastUrl.contains("test-key"), "The key must never be in the address");
        assertEquals("test-key", transport.lastHeaders.get("x-goog-api-key"));
        assertEquals(TIMEOUT, transport.lastTimeout);
        JsonObject body = JsonParser.parseString(transport.lastBody).getAsJsonObject();
        String prompt = body.getAsJsonArray("contents").get(0).getAsJsonObject()
                .getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
        assertTrue(prompt.contains("Class: A blueprint"), "The lesson goes into the prompt");
    }

    @Test
    void openAiStyleSendsABearerTokenAndReadsTheQuestions() throws AiException {
        FakeTransport transport = FakeTransport.replying(200, AiTestData.chatReply(AiTestData.QUESTIONS_JSON));
        List<AiQuestion> questions = groq(transport).generate(REQUEST);

        assertEquals(2, questions.size());
        assertEquals(OpenAiCompatibleClient.GROQ_URL, transport.lastUrl);
        assertEquals("Bearer test-key", transport.lastHeaders.get("Authorization"));
        JsonObject body = JsonParser.parseString(transport.lastBody).getAsJsonObject();
        assertEquals(OpenAiCompatibleClient.GROQ_DEFAULT_MODEL, body.get("model").getAsString());
        assertEquals(2, body.getAsJsonArray("messages").size());
    }

    @Test
    void rateLimitIsAnError() {
        AiException e = assertThrows(AiException.class,
                () -> gemini(FakeTransport.replying(429, "{}")).generate(REQUEST));
        assertTrue(e.getMessage().contains("429"));
        assertTrue(e.getMessage().contains("rate limit"));
    }

    @Test
    void wrongKeyOrModelGivesAHint() {
        AiException badKey = assertThrows(AiException.class,
                () -> gemini(FakeTransport.replying(400, "{}")).generate(REQUEST));
        assertEquals("Gemini returned HTTP 400 (check the key in config.properties).", badKey.getMessage());
        AiException unauthorized = assertThrows(AiException.class,
                () -> groq(FakeTransport.replying(401, "{}")).generate(REQUEST));
        assertTrue(unauthorized.getMessage().contains("check the key"));
        AiException noModel = assertThrows(AiException.class,
                () -> gemini(FakeTransport.replying(404, "{}")).generate(REQUEST));
        assertTrue(noModel.getMessage().contains("check the model name"));
    }

    @Test
    void serverErrorIsAnError() {
        AiException e = assertThrows(AiException.class,
                () -> groq(FakeTransport.replying(500, "oops")).generate(REQUEST));
        assertTrue(e.getMessage().contains("HTTP 500"));
    }

    @Test
    void timeoutAndNoConnectionAreErrors() {
        AiException slow = assertThrows(AiException.class,
                () -> gemini(FakeTransport.failing(new HttpTimeoutException("slow"))).generate(REQUEST));
        assertTrue(slow.getMessage().contains("too long"));
        AiException offline = assertThrows(AiException.class,
                () -> groq(FakeTransport.failing(new ConnectException("no wifi"))).generate(REQUEST));
        assertTrue(offline.getMessage().contains("could not be reached"));
        assertThrows(AiException.class,
                () -> groq(FakeTransport.failing(new IOException("reset"))).generate(REQUEST));
    }

    @Test
    void bodyThatIsNotJsonIsAnError() {
        assertThrows(AiException.class, () -> gemini(FakeTransport.replying(200, "<html>")).generate(REQUEST));
    }

    @Test
    void replyWithoutTextIsAnError() {
        assertThrows(AiException.class, () -> gemini(FakeTransport.replying(200, "{\"candidates\": []}")).generate(REQUEST));
        assertThrows(AiException.class, () -> groq(FakeTransport.replying(200, "{\"choices\": []}")).generate(REQUEST));
    }

    @Test
    void modelTextThatIsNotQuestionsIsAnError() {
        assertThrows(AiException.class,
                () -> gemini(FakeTransport.replying(200, AiTestData.geminiReply("I cannot help."))).generate(REQUEST));
        assertThrows(AiException.class,
                () -> groq(FakeTransport.replying(200, AiTestData.chatReply("{\"questions\": []}"))).generate(REQUEST));
    }

    @Test
    void blankKeysAreRejected() {
        FakeTransport transport = FakeTransport.replying(200, "{}");
        assertThrows(IllegalArgumentException.class,
                () -> new GeminiClient(" ", GeminiClient.DEFAULT_MODEL, transport, TIMEOUT));
        assertThrows(IllegalArgumentException.class,
                () -> new OpenAiCompatibleClient("Groq", OpenAiCompatibleClient.GROQ_URL, "", "m", transport, TIMEOUT));
    }

    @Test
    void promptAsksForTheRightKindAndCount() {
        String prompt = PromptBuilder.build(AiTestData.request(AiQuestionKind.MULTIPLE_CHOICE, 7));
        assertTrue(prompt.contains("exactly 7 multiple choice questions"));
        assertTrue(prompt.contains("exactly 4 different options"));
        assertTrue(prompt.contains("Pillars of OOP: Encapsulation, Inheritance, Polymorphism, Abstraction"));
        assertTrue(prompt.contains("Use ONLY the lesson"));
    }
}
