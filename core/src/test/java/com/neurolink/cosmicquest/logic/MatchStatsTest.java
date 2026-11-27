package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.Question;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchStatsTest {

    private final MatchStats stats = new MatchStats();
    private final Question field = new IdentificationQuestion("Stores data.", "Field", 1);
    private final Question method = new IdentificationQuestion("A block of code.", "Method", 1);

    private static TurnResult right(Question q, int dealt) {
        return new TurnResult(q, q.getCorrectAnswer(), true, false, 1.0, dealt, 0, BattleState.PLAYER_ATTACK);
    }

    private static TurnResult wrong(Question q, String answer, int taken) {
        return new TurnResult(q, answer, false, false, 0.0, 0, taken, BattleState.ENEMY_ATTACK);
    }

    @Test
    void countsAnswersAndDamage() {
        stats.recordAnswer(right(field, 22));
        stats.recordAnswer(wrong(method, "Class", 8));
        stats.recordAnswer(right(method, 24));
        assertEquals(2, stats.getCorrectAnswers());
        assertEquals(3, stats.getTotalAnswers());
        assertEquals(46, stats.getDamageDealt());
        assertEquals(8, stats.getDamageTaken());
        assertEquals(67, stats.getAccuracyPercent());
    }

    @Test
    void accuracyIsZeroBeforeAnyAnswer() {
        assertEquals(0, stats.getAccuracyPercent());
    }

    @Test
    void timeoutCountsAsAMissWithNoAnswer() {
        stats.recordAnswer(new TurnResult(field, null, false, true, 0.0, 0, 8, BattleState.ENEMY_ATTACK));
        assertEquals(1, stats.getMisses().size());
        assertEquals("", stats.getMisses().get(0).getPlayerAnswer());
    }

    @Test
    void partialAnswerCountsAsAMissButKeepsItsDamage() {
        stats.recordAnswer(new TurnResult(field, "a, b", false, false, 0.5, 12, 13, BattleState.ENEMY_ATTACK));
        assertEquals(0, stats.getCorrectAnswers());
        assertEquals(12, stats.getDamageDealt());
        assertEquals(1, stats.getMisses().size());
    }

    @Test
    void questionMissedTwiceIsListedOnceWithTheLatestAnswer() {
        stats.recordAnswer(wrong(field, "Method", 8));
        stats.recordAnswer(wrong(method, "Class", 8));
        stats.recordAnswer(wrong(field, "Object", 8));
        List<MatchStats.Miss> misses = stats.getMisses();
        assertEquals(2, misses.size());
        assertSame(field, misses.get(0).getQuestion(), "Order is first missed");
        assertEquals("Object", misses.get(0).getPlayerAnswer());
        assertSame(method, misses.get(1).getQuestion());
    }

    @Test
    void lossEarnsNoStars() {
        assertEquals(0, stats.getStars(false, 0, 100));
    }

    @Test
    void winWithLowHullEarnsOneStar() {
        stats.recordAnswer(wrong(field, "x", 60));
        assertEquals(1, stats.getStars(true, 40, 100));
        assertEquals(1, stats.getStars(true, 50, 100), "Exactly 50% is not above 50%");
    }

    @Test
    void winAboveHalfHullWithMissesEarnsTwoStars() {
        stats.recordAnswer(wrong(field, "x", 8));
        assertEquals(2, stats.getStars(true, 92, 100));
    }

    @Test
    void perfectWinEarnsThreeStars() {
        stats.recordAnswer(right(field, 22));
        assertEquals(3, stats.getStars(true, 100, 100));
    }

    @Test
    void missesListCannotBeChanged() {
        stats.recordAnswer(wrong(field, "x", 8));
        assertThrows(UnsupportedOperationException.class, () -> stats.getMisses().clear());
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> stats.recordAnswer(null));
        assertThrows(IllegalArgumentException.class, () -> stats.getStars(true, 0, 0));
    }

    @Test
    void emptyStatsHaveNoMisses() {
        assertTrue(stats.getMisses().isEmpty());
    }
}
