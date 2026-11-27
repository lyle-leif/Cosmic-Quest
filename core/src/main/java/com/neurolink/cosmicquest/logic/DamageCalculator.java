package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Entity;

/**
 * Works out how much damage each hit deals (PRD section 7.3).
 *
 * <pre>
 * player damage = BASE_DAMAGE x difficulty multiplier x (1 + streak bonus), rounded
 * </pre>
 *
 * <p>The streak passed in is the streak <b>after</b> counting the current
 * correct answer (PRD decision D8): the first correct answer has streak 1 and
 * already earns +10%. The bonus is capped at +50%, reached at streak 5.
 *
 * <p>This is a utility class: every method is static and it cannot be created.
 */
public final class DamageCalculator {

    /** Damage of a correct answer at difficulty 1 with no streak. */
    public static final int BASE_DAMAGE = 20;

    /** Bonus per correct answer in a row, in percent. */
    public static final int STREAK_BONUS_PERCENT = 10;

    /** Highest streak bonus, in percent. */
    public static final int MAX_STREAK_BONUS_PERCENT = 50;

    private static final double[] DIFFICULTY_MULTIPLIERS = {1.0, 1.25, 1.5};
    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 3;
    private static final int PERCENT = 100;
    private static final int MIN_PARTIAL_DAMAGE = 1;

    private DamageCalculator() {
        // No objects: use the static methods.
    }

    /**
     * Damage of a fully correct answer.
     *
     * @param difficulty the question's difficulty, 1 to 3.
     * @param streak correct answers in a row, counting this one.
     * @return the damage, rounded to a whole number.
     * @throws IllegalArgumentException if difficulty is not 1 to 3 or streak is negative.
     */
    public static int playerDamage(int difficulty, int streak) {
        return (int) Math.round(rawDamage(difficulty, streak));
    }

    /**
     * Damage of an enumeration answer, scaled by how much of the list was
     * named. Naming nothing deals 0; naming anything deals at least 1
     * (PRD decision D5).
     *
     * @param difficulty the question's difficulty, 1 to 3.
     * @param streak correct answers in a row, counting this one.
     * @param ratio the share of items named, from 0.0 to 1.0.
     * @return the damage, rounded to a whole number.
     * @throws IllegalArgumentException if any argument is out of range.
     */
    public static int enumerationDamage(int difficulty, int streak, double ratio) {
        if (Double.isNaN(ratio) || ratio < 0.0 || ratio > 1.0) {
            throw new IllegalArgumentException("Ratio must be 0.0 to 1.0, got " + ratio + ".");
        }
        double damage = rawDamage(difficulty, streak) * ratio;
        if (ratio == 0.0) {
            return 0;
        }
        return Math.max(MIN_PARTIAL_DAMAGE, (int) Math.round(damage));
    }

    /**
     * Damage an alien deals when the player answers wrong or runs out of
     * time. Special moves are added by the battle, not here.
     *
     * @param entity the attacking alien.
     * @return the alien's attack power.
     * @throws IllegalArgumentException if entity is null.
     */
    public static int enemyDamage(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The entity must not be null.");
        }
        return entity.getAttackPower();
    }

    /**
     * Returns the streak bonus in percent: 10 per streak, at most 50.
     *
     * @param streak correct answers in a row, counting this one.
     * @return the bonus, from 0 to 50.
     * @throws IllegalArgumentException if streak is negative.
     */
    public static int streakBonusPercent(int streak) {
        if (streak < 0) {
            throw new IllegalArgumentException("Streak must not be negative, got " + streak + ".");
        }
        return Math.min(streak * STREAK_BONUS_PERCENT, MAX_STREAK_BONUS_PERCENT);
    }

    /**
     * Unrounded damage. The bonus is kept in whole percent so the maths stays
     * exact (20 x 1.25 x 130 / 100 is exactly 32.5).
     */
    private static double rawDamage(int difficulty, int streak) {
        if (difficulty < MIN_DIFFICULTY || difficulty > MAX_DIFFICULTY) {
            throw new IllegalArgumentException("Difficulty must be 1 to 3, got " + difficulty + ".");
        }
        double multiplier = DIFFICULTY_MULTIPLIERS[difficulty - 1];
        return BASE_DAMAGE * multiplier * (PERCENT + streakBonusPercent(streak)) / PERCENT;
    }
}
