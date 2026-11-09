package com.neurolink.cosmicquest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MultipleChoiceQuestionTest {

    private static final List<String> OPTIONS =
            List.of("Class", "Object", "Method", "Field");

    // The right option is C (index 2): Method.
    private final MultipleChoiceQuestion question = new MultipleChoiceQuestion(
            "A block of code that performs a specific task.", OPTIONS, 2, 1);

    @Test
    void rightLetterInAnyCaseIsCorrect() {
        assertTrue(question.checkAnswer("c"));
        assertTrue(question.checkAnswer(" C "));
    }

    @Test
    void rightOptionTextIsCorrect() {
        assertTrue(question.checkAnswer("method"));
    }

    @Test
    void wrongLetterOrTextIsIncorrect() {
        assertFalse(question.checkAnswer("a"));
        assertFalse(question.checkAnswer("Field"));
    }

    @Test
    void letterOutsideAToDIsIncorrect() {
        assertFalse(question.checkAnswer("e"));
    }

    @Test
    void correctAnswerIsTheRightOptionsText() {
        assertEquals("Method", question.getCorrectAnswer());
        assertEquals(2, question.getCorrectIndex());
    }

    @Test
    void rejectsWrongNumberOfOptions() {
        assertThrows(IllegalArgumentException.class,
                () -> new MultipleChoiceQuestion("q", List.of("a", "b", "c"), 0, 1));
    }

    @Test
    void rejectsIndexOutsideZeroToThree() {
        assertThrows(IllegalArgumentException.class, () -> new MultipleChoiceQuestion("q", OPTIONS, 4, 1));
        assertThrows(IllegalArgumentException.class, () -> new MultipleChoiceQuestion("q", OPTIONS, -1, 1));
    }

    @Test
    void optionsCannotBeChanged() {
        assertThrows(UnsupportedOperationException.class, () -> question.getOptions().add("x"));
    }
}
