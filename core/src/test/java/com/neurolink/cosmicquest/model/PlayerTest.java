package com.neurolink.cosmicquest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerTest {

    private final Player player = new Player("Lyle");

    @Test
    void startsWithFullHullAndNothingElse() {
        assertEquals(100, player.getHp());
        assertEquals(100, player.getMaxHp());
        assertEquals(0, player.getScore());
        assertEquals(0, player.getStreak());
        assertFalse(player.isDefeated());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Player(" "));
        assertThrows(IllegalArgumentException.class, () -> new Player(null));
    }

    @Test
    void damageNeverGoesBelowZero() {
        player.takeDamage(150);
        assertEquals(0, player.getHp());
        assertTrue(player.isDefeated());
    }

    @Test
    void healNeverGoesAboveMax() {
        player.takeDamage(10);
        player.heal(50);
        assertEquals(100, player.getHp());
    }

    @Test
    void shieldBlocksOneHit() {
        player.activateShield(1);
        player.takeDamage(30);
        assertEquals(100, player.getHp());
        assertEquals(0, player.getShieldTurns());
        player.takeDamage(30);
        assertEquals(70, player.getHp());
    }

    @Test
    void streakGrowsAndResets() {
        player.increaseStreak();
        player.increaseStreak();
        assertEquals(2, player.getStreak());
        player.resetStreak();
        assertEquals(0, player.getStreak());
    }

    @Test
    void scoreAddsUp() {
        player.addScore(20);
        player.addScore(5);
        assertEquals(25, player.getScore());
    }

    @Test
    void rejectsNegativeNumbers() {
        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> player.heal(-1));
        assertThrows(IllegalArgumentException.class, () -> player.activateShield(-1));
        assertThrows(IllegalArgumentException.class, () -> player.addScore(-1));
    }
}
