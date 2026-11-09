package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import com.neurolink.cosmicquest.model.ReasoningQuestion;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuestionGeneratorTest {

    private static final long SEED = 42;

    /** A small lesson with 5 terms and 1 list, so every type is supported. */
    private static LessonData fullLesson() {
        return new LessonData(List.of(
                new Term("Class", "A blueprint that describes the fields and methods of an object."),
                new Term("Object", "An instance of a class that contains data and can perform actions."),
                new Term("Method", "A block of code that performs a specific task."),
                new Term("Field", "A variable that stores data belonging to an object."),
                new Term("Getter", "A method used to retrieve the value of a field.")),
                Map.of("Pillars of OOP", List.of("Encapsulation", "Inheritance", "Polymorphism", "Abstraction")));
    }

    @Test
    void fullLessonSupportsAllFourTypes() {
        QuestionGenerator generator = new QuestionGenerator(fullLesson(), new Random(SEED));
        assertEquals(4, generator.getSupportedTypes().size());
    }

    @Test
    void mixedListHasEveryTypeEqually() {
        List<Question> questions = new QuestionGenerator(fullLesson(), new Random(SEED)).generateQuestions(8);
        Map<QuestionType, Integer> perType = new EnumMap<>(QuestionType.class);
        for (Question q : questions) {
            perType.merge(q.getType(), 1, Integer::sum);
        }
        assertEquals(8, questions.size());
        assertEquals(Map.of(QuestionType.IDENTIFICATION, 2, QuestionType.MULTIPLE_CHOICE, 2,
                QuestionType.ENUMERATION, 2, QuestionType.REASONING, 2), perType);
    }

    @Test
    void multipleChoiceHasFourDifferentOptionsAndRightIndex() {
        QuestionGenerator generator = new QuestionGenerator(fullLesson(), new Random(SEED));
        for (int i = 0; i < 50; i++) {
            MultipleChoiceQuestion q = generator.generateMultipleChoice();
            assertEquals(4, new HashSet<>(q.getOptions()).size());
            assertEquals(q.getCorrectAnswer(), q.getOptions().get(q.getCorrectIndex()));
        }
    }

    @Test
    void termsDoNotRepeatUntilAllAreUsed() {
        QuestionGenerator generator = new QuestionGenerator(fullLesson(), new Random(SEED));
        Set<String> answers = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            answers.add(generator.generateIdentification().getCorrectAnswer());
        }
        assertEquals(5, answers.size());
    }

    @Test
    void sameSeedGivesSameQuestions() {
        assertEquals(questionTexts(new Random(7)), questionTexts(new Random(7)));
    }

    private static List<String> questionTexts(Random random) {
        List<String> texts = new ArrayList<>();
        for (Question q : new QuestionGenerator(fullLesson(), random).generateQuestions(10)) {
            texts.add(q.getQuestionText());
        }
        return texts;
    }

    @Test
    void everyGeneratedQuestionAcceptsItsOwnAnswer() {
        QuestionGenerator generator = new QuestionGenerator(fullLesson(), new Random(SEED));
        for (QuestionType type : List.of(QuestionType.IDENTIFICATION, QuestionType.MULTIPLE_CHOICE,
                QuestionType.ENUMERATION)) {
            Question q = generator.generate(type);
            assertTrue(q.checkAnswer(q.getCorrectAnswer()), type + " should accept its own answer");
        }
        ReasoningQuestion reasoning = generator.generateReasoning();
        assertTrue(reasoning.checkAnswer(String.join(" ", reasoning.getKeywords())));
    }

    @Test
    void keywordsSkipShortCommonAndTermWords() {
        List<String> keywords = QuestionGenerator.extractKeywords(
                new Term("Class", "A blueprint that describes the fields and methods of a class."));
        assertEquals(List.of("blueprint", "describes", "fields", "methods"), keywords);
    }

    @Test
    void smallLessonSkipsUnsupportedTypes() {
        LessonData small = new LessonData(List.of(
                new Term("Class", "A blueprint for objects with fields."),
                new Term("Object", "An instance of a class holding data."),
                new Term("Field", "A variable inside an object.")), Map.of());
        QuestionGenerator generator = new QuestionGenerator(small, new Random(SEED));
        assertEquals(List.of(QuestionType.IDENTIFICATION, QuestionType.REASONING), generator.getSupportedTypes());
        assertEquals(6, generator.generateQuestions(6).size());
        assertThrows(IllegalStateException.class, generator::generateMultipleChoice);
        assertThrows(IllegalStateException.class, generator::generateEnumeration);
    }

    @Test
    void shortDefinitionIsSkippedForReasoning() {
        LessonData lesson = new LessonData(List.of(new Term("Is", "A verb.")), Map.of());
        assertFalse(new QuestionGenerator(lesson).supports(QuestionType.REASONING));
    }

    @Test
    void emptyLessonCannotMakeQuestions() {
        QuestionGenerator generator = new QuestionGenerator(new LessonData(List.of(), Map.of()));
        assertTrue(generator.generateQuestions(0).isEmpty());
        assertThrows(IllegalStateException.class, () -> generator.generateQuestions(5));
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(IllegalArgumentException.class, () -> new QuestionGenerator(null));
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionGenerator(fullLesson(), new Random(SEED)).generateQuestions(-1));
    }
}
