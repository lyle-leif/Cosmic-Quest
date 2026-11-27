package com.neurolink.cosmicquest.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiChainTest {

    private static final AiRequest REQUEST = AiTestData.request(AiQuestionKind.IDENTIFICATION, 1);
    private static final AiQuestion QUESTION =
            new AiQuestion(AiQuestionKind.IDENTIFICATION, "A blueprint.", "Class", null, null, null);

    /** A provider that answers with fixed questions, or fails, and counts its calls. */
    private static final class StubClient implements AiClient {
        private final String name;
        private final List<AiQuestion> answer;
        int calls;

        StubClient(String name, List<AiQuestion> answer) {
            this.name = name;
            this.answer = answer;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<AiQuestion> generate(AiRequest request) throws AiException {
            calls++;
            if (answer == null) {
                throw new AiException(name + " returned HTTP 429 (rate limit reached).");
            }
            return answer;
        }
    }

    @Test
    void firstProviderThatAnswersWins() throws AiException {
        StubClient gemini = new StubClient("Gemini", List.of(QUESTION));
        StubClient groq = new StubClient("Groq", List.of(QUESTION));
        AiChain chain = new AiChain(List.of(gemini, groq));
        assertEquals(List.of(QUESTION), chain.generate(REQUEST));
        assertEquals("Gemini", chain.getLastSource());
        assertEquals(0, groq.calls, "Groq is not asked when Gemini answers");
    }

    @Test
    void failedProviderFallsThroughToTheNext() throws AiException {
        StubClient gemini = new StubClient("Gemini", null);
        StubClient groq = new StubClient("Groq", List.of(QUESTION));
        AiChain chain = new AiChain(List.of(gemini, groq));
        assertEquals(List.of(QUESTION), chain.generate(REQUEST));
        assertEquals("Groq", chain.getLastSource());
    }

    @Test
    void emptyAnswerAlsoFallsThrough() throws AiException {
        AiChain chain = new AiChain(List.of(new StubClient("Gemini", new ArrayList<>()),
                new StubClient("Groq", List.of(QUESTION))));
        chain.generate(REQUEST);
        assertEquals("Groq", chain.getLastSource());
    }

    @Test
    void allFailingGivesEveryReason() {
        AiChain chain = new AiChain(List.of(new StubClient("Gemini", null), new StubClient("Groq", null)));
        AiException e = assertThrows(AiException.class, () -> chain.generate(REQUEST));
        assertTrue(e.getMessage().contains("Gemini returned HTTP 429"));
        assertTrue(e.getMessage().contains("Groq returned HTTP 429"));
        assertNull(chain.getLastSource());
    }

    @Test
    void nameListsEveryProvider() {
        AiChain chain = new AiChain(List.of(new StubClient("Gemini", null), new StubClient("Groq", null)));
        assertEquals("Gemini, Groq", chain.getName());
    }

    @Test
    void rejectsEmptyOrNullProviders() {
        assertThrows(IllegalArgumentException.class, () -> new AiChain(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new AiChain(null));
        List<AiClient> withNull = Arrays.asList(new StubClient("Gemini", null), null);
        assertThrows(IllegalArgumentException.class, () -> new AiChain(withNull));
    }
}
