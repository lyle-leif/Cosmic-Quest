package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.ai.AiChain;
import com.neurolink.cosmicquest.ai.AiClient;
import com.neurolink.cosmicquest.ai.AiException;
import com.neurolink.cosmicquest.ai.AiQuestion;
import com.neurolink.cosmicquest.ai.AiQuestionKind;
import com.neurolink.cosmicquest.ai.AiRequest;
import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.QuestionType;
import com.neurolink.cosmicquest.model.ReasoningQuestion;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** The game's side of AI questions, with fake AI clients only (no network). */
class AiQuestionProviderTest {

    private static final LessonData LESSON = new LessonData(List.of(
            new Term("Class", "A blueprint that describes the fields and methods of an object."),
            new Term("Object", "An instance of a class that contains data and can perform actions."),
            new Term("Method", "A block of code that performs a specific task."),
            new Term("Field", "A variable that stores data belonging to an object.")),
            Map.of("Pillars of OOP", List.of("Encapsulation", "Inheritance", "Polymorphism", "Abstraction")));

    /** A fake AI that answers with fixed questions (or fails) and remembers the request. */
    private static class FakeAi implements AiClient {
        private final List<AiQuestion> answer;
        AiRequest lastRequest;

        FakeAi(List<AiQuestion> answer) {
            this.answer = answer;
        }

        @Override
        public String getName() {
            return "FakeAI";
        }

        @Override
        public List<AiQuestion> generate(AiRequest request) throws AiException {
            lastRequest = request;
            if (answer == null) {
                throw new AiException("FakeAI could not be reached.");
            }
            return answer;
        }
    }

    private static LocalProvider local() {
        return new LocalProvider(new QuestionGenerator(LESSON, new Random(42)));
    }

    private static AiQuestion id(String text, String answer) {
        return new AiQuestion(AiQuestionKind.IDENTIFICATION, text, answer, null, null, null);
    }

    private static List<AiQuestion> identification(int count) {
        List<AiQuestion> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            questions.add(id("A blueprint for objects, clue " + i + ".", "Class"));
        }
        return questions;
    }

    @Test
    void usesTheAiQuestionsAndSaysWhereTheyCameFrom() {
        FakeAi ai = new FakeAi(identification(10));
        AiQuestionProvider provider = new AiQuestionProvider(ai, LESSON, local());
        List<Question> questions = provider.provide(QuestionType.IDENTIFICATION, 10);
        assertEquals(10, questions.size());
        assertEquals("A blueprint for objects, clue 0.", questions.get(0).getQuestionText());
        assertEquals(QuestionGenerator.IDENTIFICATION_DIFFICULTY, questions.get(0).getDifficulty());
        assertEquals("AI questions from FakeAI", provider.getStatus());
        assertNull(provider.getLastProblem());
    }

    @Test
    void sendsTheLessonAndKindToTheAi() {
        FakeAi ai = new FakeAi(identification(3));
        new AiQuestionProvider(ai, LESSON, local()).provide(QuestionType.IDENTIFICATION, 3);
        assertEquals(AiQuestionKind.IDENTIFICATION, ai.lastRequest.getKind());
        assertEquals(3, ai.lastRequest.getCount());
        assertEquals(4, ai.lastRequest.getTerms().size());
        assertTrue(ai.lastRequest.getLists().containsKey("Pillars of OOP"));
    }

    @Test
    void failedAiGivesLocalQuestionsAndTheOfflineMessage() {
        AiQuestionProvider provider = new AiQuestionProvider(new FakeAi(null), LESSON, local());
        List<Question> questions = provider.provide(QuestionType.IDENTIFICATION, 10);
        assertEquals(10, questions.size(), "The battle still gets every question");
        assertEquals(AiQuestionProvider.OFFLINE_STATUS, provider.getStatus());
        assertEquals("Offline: using local questions", provider.getStatus());
        assertEquals("FakeAI could not be reached.", provider.getLastProblem());
    }

    @Test
    void unexpectedCrashInTheAiStillFallsBack() {
        AiClient broken = new FakeAi(List.of()) {
            @Override
            public List<AiQuestion> generate(AiRequest request) {
                throw new IllegalStateException("bug inside the AI client");
            }
        };
        AiQuestionProvider provider = new AiQuestionProvider(broken, LESSON, local());
        assertEquals(4, provider.provide(QuestionType.IDENTIFICATION, 4).size());
        assertEquals(AiQuestionProvider.OFFLINE_STATUS, provider.getStatus());
        assertEquals("bug inside the AI client", provider.getLastProblem());
    }

    @Test
    void shortfallIsToppedUpWithLocalQuestions() {
        AiQuestionProvider provider = new AiQuestionProvider(new FakeAi(identification(4)), LESSON, local());
        List<Question> questions = provider.provide(QuestionType.IDENTIFICATION, 10);
        assertEquals(10, questions.size());
        assertTrue(provider.getStatus().contains("topped up with local questions"));
    }

    @Test
    void neverReturnsMoreThanAskedFor() {
        AiQuestionProvider provider = new AiQuestionProvider(new FakeAi(identification(15)), LESSON, local());
        assertEquals(5, provider.provide(QuestionType.IDENTIFICATION, 5).size());
    }

    @Test
    void questionsThatBreakTheRulesAreDropped() {
        List<AiQuestion> bad = List.of(
                id("", "Class"),
                id("A blueprint.", " "),
                id("A Class is a blueprint.", "class"));
        AiQuestionProvider provider = new AiQuestionProvider(new FakeAi(bad), LESSON, local());
        assertEquals(3, provider.provide(QuestionType.IDENTIFICATION, 3).size());
        assertEquals(AiQuestionProvider.OFFLINE_STATUS, provider.getStatus(), "None of them was usable");
        assertNotNull(provider.getLastProblem());
    }

    @Test
    void multipleChoiceNeedsFourDifferentOptionsIncludingTheAnswer() {
        AiQuestion good = new AiQuestion(AiQuestionKind.MULTIPLE_CHOICE, "Which is a blueprint?", "class",
                List.of("Object", "Class", "Method", "Field"), null, null);
        MultipleChoiceQuestion q = assertInstanceOf(MultipleChoiceQuestion.class,
                AiQuestionProvider.convert(good, QuestionType.MULTIPLE_CHOICE));
        assertEquals(1, q.getCorrectIndex());
        assertTrue(q.checkAnswer("B"));

        assertNull(convertMc("Class", List.of("Object", "Class", "Method")), "Only 3 options");
        assertNull(convertMc("Class", List.of("Object", "Class", "class", "Field")), "Repeated option");
        assertNull(convertMc("Package", List.of("Object", "Class", "Method", "Field")), "Answer not an option");
    }

    private static Question convertMc(String answer, List<String> options) {
        return AiQuestionProvider.convert(new AiQuestion(AiQuestionKind.MULTIPLE_CHOICE, "Pick one.",
                answer, options, null, null), QuestionType.MULTIPLE_CHOICE);
    }

    @Test
    void enumerationNeedsAtLeastTwoDifferentItems() {
        AiQuestion good = new AiQuestion(AiQuestionKind.ENUMERATION, "Name the pillars of OOP.", "",
                null, List.of("Encapsulation", "Inheritance", "encapsulation"), null);
        EnumerationQuestion q = assertInstanceOf(EnumerationQuestion.class,
                AiQuestionProvider.convert(good, QuestionType.ENUMERATION));
        assertEquals(List.of("Encapsulation", "Inheritance"), q.getItems(), "Repeated items are removed");

        AiQuestion one = new AiQuestion(AiQuestionKind.ENUMERATION, "Name one.", "", null, List.of("Only"), null);
        assertNull(AiQuestionProvider.convert(one, QuestionType.ENUMERATION));
    }

    @Test
    void reasoningKeepsOnlySingleWordKeywords() {
        AiQuestion good = new AiQuestion(AiQuestionKind.REASONING, "Why use encapsulation?",
                "It protects data by hiding fields behind methods.", null, null,
                List.of("Data", "hiding fields", "methods", "protects"));
        ReasoningQuestion q = assertInstanceOf(ReasoningQuestion.class,
                AiQuestionProvider.convert(good, QuestionType.REASONING));
        assertEquals(List.of("data", "methods", "protects"), q.getKeywords());

        AiQuestion tooFew = new AiQuestion(AiQuestionKind.REASONING, "Why?", "Because.", null, null,
                List.of("because"));
        assertNull(AiQuestionProvider.convert(tooFew, QuestionType.REASONING));
    }

    @Test
    void supportsOnlyWhatTheLessonCanMakeLocally() {
        LessonData termsOnly = new LessonData(List.of(new Term("Class", "A blueprint for objects.")), Map.of());
        AiQuestionProvider provider = new AiQuestionProvider(new FakeAi(identification(1)), termsOnly,
                new LocalProvider(new QuestionGenerator(termsOnly, new Random(1))));
        assertTrue(provider.supports(QuestionType.IDENTIFICATION));
        assertFalse(provider.supports(QuestionType.ENUMERATION));
        assertThrows(IllegalStateException.class, () -> provider.provide(QuestionType.ENUMERATION, 1));
    }

    @Test
    void chainNameShowsWhichProviderAnswered() {
        AiClient groq = new FakeAi(identification(2)) {
            @Override
            public String getName() {
                return "Groq";
            }
        };
        AiClient gemini = new FakeAi(null);
        AiQuestionProvider provider = new AiQuestionProvider(new AiChain(List.of(gemini, groq)), LESSON, local());
        provider.provide(QuestionType.IDENTIFICATION, 2);
        assertEquals("AI questions from Groq", provider.getStatus());
    }

    @Test
    void battlePlaysWithAiQuestionsAndReportsTheirSource() {
        AiQuestionProvider provider = new AiQuestionProvider(new FakeAi(identification(10)), LESSON, local());
        SoloBossBattle battle = new SoloBossBattle(new Player("Lyle"), new VoidBrute(1), provider, new Random(42));
        battle.startBattle();
        assertTrue(battle.getCurrentQuestion().getQuestionText().startsWith("A blueprint for objects, clue"));
        assertEquals("AI questions from FakeAI", battle.getQuestionStatus());
        assertNull(battle.getQuestionProblem());
        assertTrue(battle.submitAnswer("Class").isCorrect());
    }

    @Test
    void battleStillPlaysWhenTheAiIsDown() {
        AiQuestionProvider provider = new AiQuestionProvider(new FakeAi(null), LESSON, local());
        SoloBossBattle battle = new SoloBossBattle(new Player("Lyle"), new VoidBrute(1), provider, new Random(42));
        battle.startBattle();
        assertEquals(AiQuestionProvider.OFFLINE_STATUS, battle.getQuestionStatus());
        assertEquals("FakeAI could not be reached.", battle.getQuestionProblem());
        assertTrue(battle.submitAnswer(battle.getCurrentQuestion().getCorrectAnswer()).isCorrect());
    }

    @Test
    void localProviderMakesExactlyTheCountAndRejectsNull() {
        LocalProvider local = local();
        assertEquals(7, local.provide(QuestionType.MULTIPLE_CHOICE, 7).size());
        assertEquals(LocalProvider.STATUS, local.getStatus());
        assertThrows(IllegalArgumentException.class, () -> local.provide(QuestionType.IDENTIFICATION, -1));
        assertThrows(IllegalArgumentException.class, () -> new LocalProvider(null));
        assertThrows(IllegalArgumentException.class, () -> new AiQuestionProvider(null, LESSON, local));
    }
}
