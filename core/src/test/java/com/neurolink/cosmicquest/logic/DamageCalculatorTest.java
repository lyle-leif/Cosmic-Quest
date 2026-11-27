package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.VoidBrute;
import org.junit.jupiter.api.Test;

class DamageCalculatorTest {

    @Test
    void baseDamageWithNoStreak() {
        assertEquals(20, DamageCalculator.playerDamage(1, 0));
        assertEquals(25, DamageCalculator.playerDamage(2, 0));
        assertEquals(30, DamageCalculator.playerDamage(3, 0));
    }

    @Test
    void firstCorrectAnswerAlreadyGetsTenPercent() {
        // Decision D8: streak counts the current answer, so the first hit is streak 1.
        assertEquals(22, DamageCalculator.playerDamage(1, 1));
    }

    @Test
    void streakAddsTenPercentEach() {
        assertEquals(26, DamageCalculator.playerDamage(1, 3));
        assertEquals(33, DamageCalculator.playerDamage(2, 3)); // 32.5 rounds up
    }

    @Test
    void streakBonusIsCappedAtFiftyPercent() {
        assertEquals(45, DamageCalculator.playerDamage(3, 5));
        assertEquals(45, DamageCalculator.playerDamage(3, 10));
        assertEquals(50, DamageCalculator.streakBonusPercent(99));
    }

    @Test
    void streakBonusPercentSteps() {
        assertEquals(0, DamageCalculator.streakBonusPercent(0));
        assertEquals(10, DamageCalculator.streakBonusPercent(1));
        assertEquals(40, DamageCalculator.streakBonusPercent(4));
        assertEquals(50, DamageCalculator.streakBonusPercent(5));
    }

    @Test
    void enumerationScalesByRatio() {
        assertEquals(19, DamageCalculator.enumerationDamage(2, 0, 0.75)); // 18.75
        assertEquals(25, DamageCalculator.enumerationDamage(2, 0, 1.0));
    }

    @Test
    void enumerationFullRatioMatchesPlayerDamage() {
        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            for (int streak = 0; streak <= 6; streak++) {
                assertEquals(DamageCalculator.playerDamage(difficulty, streak),
                        DamageCalculator.enumerationDamage(difficulty, streak, 1.0));
            }
        }
    }

    @Test
    void enumerationNothingNamedDealsZero() {
        assertEquals(0, DamageCalculator.enumerationDamage(3, 5, 0.0));
    }

    @Test
    void enumerationTinyRatioDealsAtLeastOne() {
        assertEquals(1, DamageCalculator.enumerationDamage(1, 0, 0.01)); // 0.2 would round to 0
    }

    @Test
    void enemyDamageIsAttackPower() {
        assertEquals(8, DamageCalculator.enemyDamage(new VoidBrute(1)));
        assertEquals(22, DamageCalculator.enemyDamage(new LunarBeast(3)));
    }

    @Test
    void rejectsDifficultyOutsideOneToThree() {
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.playerDamage(0, 0));
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.playerDamage(4, 0));
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.enumerationDamage(4, 0, 0.5));
    }

    @Test
    void rejectsNegativeStreak() {
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.playerDamage(1, -1));
    }

    @Test
    void rejectsRatioOutsideZeroToOne() {
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.enumerationDamage(1, 0, -0.1));
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.enumerationDamage(1, 0, 1.5));
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.enumerationDamage(1, 0, Double.NaN));
    }

    @Test
    void rejectsNullEntity() {
        assertThrows(IllegalArgumentException.class, () -> DamageCalculator.enemyDamage(null));
    }
}
