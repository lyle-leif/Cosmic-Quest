package com.neurolink.cosmicquest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IdentificationQuestionTest {

    private final Question question = new IdentificationQuestion(
            "Bundling data and methods while hiding details.", "Encapsulation", 1);

    @Test
    void exactAnswerIsCorrect() {
        assertTrue(question.checkAnswer("Encapsulation"));
    }

    @Test
    void ignoresCaseAndExtraSpaces() {
        assertTrue(question.checkAnswer("   ENCAPSULATION  "));
    }

    @Test
    void wrongAnswerIsIncorrect() {
        assertFalse(question.checkAnswer("Inheritance"));
    }

    @Test
    void nullOrBlankAnswerIsIncorrect() {
        assertFalse(question.checkAnswer(null));
        assertFalse(question.checkAnswer("   "));
    }

    @Test
    void reportsItsType() {
        assertEquals(QuestionType.IDENTIFICATION, question.getType());
    }

    @Test
    void rejectsDifficultyOutsideOneToThree() {
        assertThrows(IllegalArgumentException.class, () -> new IdentificationQuestion("text", "answer", 0));
        assertThrows(IllegalArgumentException.class, () -> new IdentificationQuestion("text", "answer", 4));
    }

    @Test
    void rejectsBlankTextOrAnswer() {
        assertThrows(IllegalArgumentException.class, () -> new IdentificationQuestion(" ", "answer", 1));
        assertThrows(IllegalArgumentException.class, () -> new IdentificationQuestion("text", null, 1));
    }
}
