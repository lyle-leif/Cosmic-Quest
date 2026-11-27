package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.Question;
import org.junit.jupiter.api.Test;

class TurnResultTest {

    private final Question question = new IdentificationQuestion("Stores data.", "Field", 1);

    @Test
    void keepsEveryValue() {
        TurnResult result = new TurnResult(question, "field", true, false, 1.0, 22, 0, BattleState.PLAYER_ATTACK);
        assertSame(question, result.getQuestion());
        assertEquals("field", result.getPlayerAnswer());
        assertTrue(result.isCorrect());
        assertFalse(result.isTimedOut());
        assertEquals(1.0, result.getMatchRatio(), 0.0001);
        assertEquals(22, result.getDamageDealt());
        assertEquals(0, result.getDamageTaken());
        assertEquals(BattleState.PLAYER_ATTACK, result.getNewState());
    }

    @Test
    void timeoutStoresEmptyAnswer() {
        TurnResult result = new TurnResult(question, null, false, true, 0.0, 0, 8, BattleState.ENEMY_ATTACK);
        assertEquals("", result.getPlayerAnswer());
        assertTrue(result.isTimedOut());
    }

    @Test
    void rejectsBadValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new TurnResult(null, "x", true, false, 1.0, 0, 0, BattleState.WON));
        assertThrows(IllegalArgumentException.class,
                () -> new TurnResult(question, "x", true, false, 1.0, 0, 0, null));
        assertThrows(IllegalArgumentException.class,
                () -> new TurnResult(question, "x", true, false, 1.0, -1, 0, BattleState.WON));
        assertThrows(IllegalArgumentException.class,
                () -> new TurnResult(question, "x", true, false, 1.5, 0, 0, BattleState.WON));
    }
}
