package com.neurolink.cosmicquest.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AiQuestionParserTest {

    @Test
    void readsQuestionsAndUsesTheRequestedKind() throws AiException {
        List<AiQuestion> questions = AiQuestionParser.parse(AiTestData.QUESTIONS_JSON,
                AiQuestionKind.IDENTIFICATION, 10);
        assertEquals(2, questions.size());
        assertEquals("A blueprint for objects.", questions.get(0).getText());
        assertEquals("Class", questions.get(0).getAnswer());
        assertEquals(AiQuestionKind.IDENTIFICATION, questions.get(1).getKind());
    }

    @Test
    void ignoresFencesAndExtraWordsAroundTheJson() throws AiException {
        String reply = "Sure! Here you go:\n```json\n" + AiTestData.QUESTIONS_JSON + "\n```\nGood luck!";
        assertEquals(2, AiQuestionParser.parse(reply, AiQuestionKind.IDENTIFICATION, 10).size());
    }

    @Test
    void readsListsForEachKind() throws AiException {
        String reply = "{\"questions\": [{\"text\": \"Pick one.\", \"options\": [\"A\", \"B\", \"C\", \"D\"],"
                + " \"answer\": \"B\", \"items\": [\"x\", \"y\"], \"keywords\": [\"data\", 5, \"methods\"]}]}";
        AiQuestion q = AiQuestionParser.parse(reply, AiQuestionKind.MULTIPLE_CHOICE, 10).get(0);
        assertEquals(List.of("A", "B", "C", "D"), q.getOptions());
        assertEquals(List.of("x", "y"), q.getItems());
        assertEquals(List.of("data", "5", "methods"), q.getKeywords());
    }

    @Test
    void keepsAtMostTheRequestedNumber() throws AiException {
        assertEquals(1, AiQuestionParser.parse(AiTestData.QUESTIONS_JSON, AiQuestionKind.IDENTIFICATION, 1).size());
    }

    @Test
    void skipsEntriesThatAreNotObjectsAndFillsMissingFields() throws AiException {
        String reply = "{\"questions\": [\"oops\", 7, {\"text\": \"Only text\"}]}";
        List<AiQuestion> questions = AiQuestionParser.parse(reply, AiQuestionKind.REASONING, 10);
        assertEquals(1, questions.size());
        assertEquals("", questions.get(0).getAnswer());
        assertTrue(questions.get(0).getKeywords().isEmpty());
    }

    @Test
    void badRepliesThrow() {
        assertThrows(AiException.class, () -> AiQuestionParser.parse(null, AiQuestionKind.IDENTIFICATION, 5));
        assertThrows(AiException.class, () -> AiQuestionParser.parse("no json here", AiQuestionKind.IDENTIFICATION, 5));
        assertThrows(AiException.class, () -> AiQuestionParser.parse("{\"questions\": [", AiQuestionKind.IDENTIFICATION, 5));
        assertThrows(AiException.class, () -> AiQuestionParser.parse("{\"items\": []}", AiQuestionKind.IDENTIFICATION, 5));
    }
}
