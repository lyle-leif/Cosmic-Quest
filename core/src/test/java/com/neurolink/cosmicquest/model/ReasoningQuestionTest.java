package com.neurolink.cosmicquest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReasoningQuestionTest {

    private static final double DELTA = 0.0001;

    private final ReasoningQuestion question = new ReasoningQuestion("Explain Class in your own words.",
            "A blueprint that defines the fields and methods of objects.",
            List.of("blueprint", "objects", "fields", "methods", "class"), 3);

    @Test
    void threeOfFiveKeywordsPasses() {
        assertTrue(question.checkAnswer("It is a blueprint for objects with methods."));
    }

    @Test
    void twoOfFiveKeywordsFails() {
        assertFalse(question.checkAnswer("A blueprint for objects."));
    }

    @Test
    void keywordMustBeAWholeWord() {
        assertEquals(0.0, question.getKeywordRatio("a subclass"), DELTA);
    }

    @Test
    void caseAndPunctuationAreIgnored() {
        assertEquals(1.0, question.getKeywordRatio("BLUEPRINT! Objects, FIELDS; methods... class?"), DELTA);
    }

    @Test
    void nullAnswerFails() {
        assertFalse(question.checkAnswer(null));
    }

    @Test
    void passRatioIsSixtyPercent() {
        assertEquals(0.6, ReasoningQuestion.KEYWORD_PASS_RATIO, DELTA);
        assertEquals(QuestionType.REASONING, question.getType());
    }

    @Test
    void rejectsMissingKeywords() {
        assertThrows(IllegalArgumentException.class, () -> new ReasoningQuestion("q", "a", List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> new ReasoningQuestion("q", "a", null, 1));
    }

    @Test
    void keywordsCannotBeChanged() {
        assertThrows(UnsupportedOperationException.class, () -> question.getKeywords().add("x"));
    }
}
