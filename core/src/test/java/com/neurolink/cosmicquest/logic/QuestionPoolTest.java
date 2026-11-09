package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.Question;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class QuestionPoolTest {

    private static final long SEED = 42;

    private static List<Question> makeQuestions(int count) {
        List<Question> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(new IdentificationQuestion("Definition " + i, "Term " + i, 1));
        }
        return list;
    }

    @Test
    void handsOutEveryQuestionOnce() {
        List<Question> source = makeQuestions(5);
        QuestionPool pool = new QuestionPool(source, new Random(SEED));
        List<Question> drawn = new ArrayList<>();
        while (pool.hasNext()) {
            drawn.add(pool.getNextQuestion());
        }
        assertEquals(5, drawn.size());
        assertEquals(new HashSet<>(source), new HashSet<>(drawn));
    }

    @Test
    void shufflesTheQuestions() {
        List<Question> source = makeQuestions(10);
        QuestionPool pool = new QuestionPool(source, new Random(SEED));
        List<Question> drawn = new ArrayList<>();
        while (pool.hasNext()) {
            drawn.add(pool.getNextQuestion());
        }
        assertNotEquals(source, drawn);
    }

    @Test
    void sameSeedGivesSameOrder() {
        List<Question> source = makeQuestions(6);
        QuestionPool first = new QuestionPool(source, new Random(7));
        QuestionPool second = new QuestionPool(source, new Random(7));
        while (first.hasNext()) {
            assertSame(first.getNextQuestion(), second.getNextQuestion());
        }
    }

    @Test
    void missedQuestionComesBackLast() {
        QuestionPool pool = new QuestionPool(makeQuestions(3), new Random(SEED));
        Question missed = pool.getNextQuestion();
        pool.recordMissedQuestion(missed);
        assertEquals(3, pool.size());
        pool.getNextQuestion();
        pool.getNextQuestion();
        assertSame(missed, pool.getNextQuestion());
        assertFalse(pool.hasNext());
    }

    @Test
    void repeatedMissIsListedOnce() {
        QuestionPool pool = new QuestionPool(makeQuestions(2), new Random(SEED));
        Question q = pool.getNextQuestion();
        pool.recordMissedQuestion(q);
        pool.getNextQuestion();
        assertSame(q, pool.getNextQuestion());
        pool.recordMissedQuestion(q);
        assertEquals(List.of(q), pool.getMissedQuestions());
    }

    @Test
    void missedListKeepsOrderFirstMissed() {
        QuestionPool pool = new QuestionPool(makeQuestions(3), new Random(SEED));
        Question a = pool.getNextQuestion();
        Question b = pool.getNextQuestion();
        pool.recordMissedQuestion(b);
        pool.recordMissedQuestion(a);
        assertEquals(List.of(b, a), pool.getMissedQuestions());
    }

    @Test
    void emptyPoolThrowsClearError() {
        QuestionPool pool = new QuestionPool(List.of(), new Random(SEED));
        assertFalse(pool.hasNext());
        assertEquals(0, pool.size());
        assertThrows(IllegalStateException.class, pool::getNextQuestion);
    }

    @Test
    void refillAddsBehindWaitingQuestions() {
        List<Question> firstBatch = makeQuestions(2);
        QuestionPool pool = new QuestionPool(firstBatch, new Random(SEED));
        Question extra = new IdentificationQuestion("Extra definition", "Extra", 1);
        pool.addQuestions(List.of(extra));
        assertEquals(3, pool.size());
        assertTrue(firstBatch.contains(pool.getNextQuestion()));
        assertTrue(firstBatch.contains(pool.getNextQuestion()));
        assertSame(extra, pool.getNextQuestion());
    }

    @Test
    void refillAfterRunningDry() {
        QuestionPool pool = new QuestionPool(makeQuestions(1), new Random(SEED));
        pool.getNextQuestion();
        assertFalse(pool.hasNext());
        pool.addQuestions(makeQuestions(2));
        assertTrue(pool.hasNext());
        assertEquals(2, pool.size());
    }

    @Test
    void changingTheOriginalListDoesNotChangeThePool() {
        List<Question> source = makeQuestions(2);
        QuestionPool pool = new QuestionPool(source, new Random(SEED));
        source.clear();
        assertEquals(2, pool.size());
    }

    @Test
    void missedListCannotBeChanged() {
        QuestionPool pool = new QuestionPool(makeQuestions(1), new Random(SEED));
        pool.recordMissedQuestion(pool.getNextQuestion());
        assertThrows(UnsupportedOperationException.class, () -> pool.getMissedQuestions().clear());
    }

    @Test
    void rejectsNulls() {
        assertThrows(IllegalArgumentException.class, () -> new QuestionPool(null));
        assertThrows(IllegalArgumentException.class, () -> new QuestionPool(makeQuestions(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionPool(Arrays.asList(makeQuestions(1).get(0), null)));
        QuestionPool pool = new QuestionPool(makeQuestions(1), new Random(SEED));
        assertThrows(IllegalArgumentException.class, () -> pool.recordMissedQuestion(null));
        assertThrows(IllegalArgumentException.class, () -> pool.addQuestions(null));
    }
}
