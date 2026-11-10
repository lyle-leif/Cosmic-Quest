package com.neurolink.cosmicquest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EntityTest {

    @Test
    void levelSetsHpAndAttack() {
        Entity brute = new VoidBrute(2);
        assertEquals(100, brute.getHp());
        assertEquals(100, brute.getMaxHp());
        assertEquals(12, brute.getAttackPower());
    }

    @Test
    void rejectsLevelOutsideOneToThree() {
        assertThrows(IllegalArgumentException.class, () -> new VoidBrute(0));
        assertThrows(IllegalArgumentException.class, () -> new StarWeaver(4));
        assertThrows(IllegalArgumentException.class, () -> new NebulaLich(-1));
        assertThrows(IllegalArgumentException.class, () -> new LunarBeast(5));
    }

    @Test
    void eachAlienAsksItsOwnQuestionType() {
        assertEquals(QuestionType.IDENTIFICATION, new VoidBrute(1).getQuestionType());
        assertEquals(QuestionType.REASONING, new StarWeaver(1).getQuestionType());
        assertEquals(QuestionType.MULTIPLE_CHOICE, new NebulaLich(1).getQuestionType());
        assertEquals(QuestionType.ENUMERATION, new LunarBeast(1).getQuestionType());
    }

    @Test
    void damageLowersHpButNeverBelowZero() {
        Entity beast = new LunarBeast(1);
        beast.takeDamage(30);
        assertEquals(50, beast.getHp());
        beast.takeDamage(500);
        assertEquals(0, beast.getHp());
        assertTrue(beast.isDefeated());
    }

    @Test
    void rejectsNegativeDamage() {
        assertThrows(IllegalArgumentException.class, () -> new VoidBrute(1).takeDamage(-5));
    }

    @Test
    void strikeIsAttackPowerByDefault() {
        assertEquals(10, new StarWeaver(1).getStrikeDamage(3, 2));
        assertEquals(9, new NebulaLich(1).getStrikeDamage(6, 0));
    }

    @Test
    void groundSlamHitsHarderEveryThirdTurn() {
        Entity brute = new VoidBrute(1);
        assertEquals(8, brute.getStrikeDamage(1, 0));
        assertEquals(8, brute.getStrikeDamage(2, 0));
        assertEquals(12, brute.getStrikeDamage(3, 0));
        assertEquals(8, brute.getStrikeDamage(4, 0));
        assertEquals(12, brute.getStrikeDamage(6, 0));
    }

    @Test
    void moonPounceGrowsWithEachItemMissed() {
        Entity beast = new LunarBeast(1);
        assertEquals(11, beast.getStrikeDamage(1, 0));
        assertEquals(13, beast.getStrikeDamage(1, 1));
        assertEquals(19, beast.getStrikeDamage(1, 4));
    }

    @Test
    void onlyStarWeaverLocksPowerUps() {
        assertEquals(2, new StarWeaver(1).getPowerUpLockTurns());
        assertEquals(0, new VoidBrute(1).getPowerUpLockTurns());
        assertEquals(0, new NebulaLich(1).getPowerUpLockTurns());
        assertEquals(0, new LunarBeast(1).getPowerUpLockTurns());
    }

    @Test
    void strikeRejectsNegativeNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new VoidBrute(1).getStrikeDamage(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new LunarBeast(1).getStrikeDamage(1, -1));
    }

    @Test
    void attackHitsTargetForAttackPower() {
        Entity weaver = new StarWeaver(3);
        Player player = new Player("Lyle");
        weaver.attack(player);
        assertEquals(100 - weaver.getAttackPower(), player.getHp());
        assertFalse(player.isDefeated());
    }
}
