package com.neurolink.cosmicquest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnumerationQuestionTest {

    private static final double DELTA = 0.0001;

    private final EnumerationQuestion question = new EnumerationQuestion("List the four OOP pillars.",
            List.of("Encapsulation", "Inheritance", "Polymorphism", "Abstraction"), 2);

    @Test
    void threeOfFourGivesThreeQuarters() {
        assertEquals(0.75, question.getMatchRatio("encapsulation, INHERITANCE , polymorphism"), DELTA);
        assertFalse(question.checkAnswer("encapsulation, inheritance, polymorphism"));
    }

    @Test
    void allItemsInAnyOrderIsCorrect() {
        assertTrue(question.checkAnswer("abstraction,polymorphism, inheritance ,Encapsulation"));
    }

    @Test
    void punctuationIsIgnored() {
        assertTrue(question.checkAnswer("Encapsulation., Inheritance, Polymorphism, Abstraction!"));
    }

    @Test
    void repeatedItemCountsOnce() {
        assertEquals(0.25, question.getMatchRatio("inheritance, inheritance, inheritance"), DELTA);
    }

    @Test
    void nullOrBlankGivesZero() {
        assertEquals(0.0, question.getMatchRatio(null), DELTA);
        assertEquals(0.0, question.getMatchRatio("   "), DELTA);
    }

    @Test
    void correctAnswerJoinsItems() {
        assertEquals("Encapsulation, Inheritance, Polymorphism, Abstraction", question.getCorrectAnswer());
        assertEquals(QuestionType.ENUMERATION, question.getType());
    }

    @Test
    void rejectsNullEmptyOrBlankItems() {
        assertThrows(IllegalArgumentException.class, () -> new EnumerationQuestion("q", null, 1));
        assertThrows(IllegalArgumentException.class, () -> new EnumerationQuestion("q", List.of(), 1));
        assertThrows(IllegalArgumentException.class,
                () -> new EnumerationQuestion("q", Arrays.asList("a", null), 1));
        assertThrows(IllegalArgumentException.class, () -> new EnumerationQuestion("q", List.of("a", " "), 1));
    }

    @Test
    void itemsCannotBeChanged() {
        assertThrows(UnsupportedOperationException.class, () -> question.getItems().add("x"));
    }
}
