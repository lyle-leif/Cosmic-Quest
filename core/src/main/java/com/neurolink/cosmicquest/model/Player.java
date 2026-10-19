package com.neurolink.cosmicquest.model;

/**
 * Represents the hero controlled by the person playing, tracking hull, score
 * and streak.
 */
public class Player implements Combatant {

    private static final int STARTING_HP = 100;

    private final String name;
    private final int maxHp;
    private int hp;
    private int score;
    private int streak;
    private int shieldTurns;

    /**
     * Creates a player with a full hull.
     *
     * @param name the player's name, must not be blank.
     * @throws IllegalArgumentException if the name is null or blank.
     */
    public Player(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The name must not be blank.");
        }
        this.name = name;
        this.hp = STARTING_HP;
        this.maxHp = STARTING_HP;
    }

    /** Returns the player's name. */
    public String getName() {
        return name;
    }

    /** Returns the current hull points. */
    @Override
    public int getHp() {
        return hp;
    }

    /** Returns the maximum hull points. */
    @Override
    public int getMaxHp() {
        return maxHp;
    }

    /** Returns the player's score. */
    public int getScore() {
        return score;
    }

    /** Returns how many correct answers in a row the player has. */
    public int getStreak() {
        return streak;
    }

    /** Returns how many more hits the shield will block. */
    public int getShieldTurns() {
        return shieldTurns;
    }

    /** Returns true when the hull is at zero. */
    @Override
    public boolean isDefeated() {
        return hp <= 0;
    }

    /**
     * The shield blocks the hit, otherwise hp goes down but never below 0.
     *
     * @param amount the damage the hit would deal.
     * @throws IllegalArgumentException if the amount is negative.
     */
    @Override
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        if (shieldTurns > 0) {
            shieldTurns--;
        } else {
            hp = Math.max(0, hp - amount);
        }
    }

    /**
     * Restores hull, never above the maximum.
     *
     * @param amount the number of hull points to restore.
     * @throws IllegalArgumentException if the amount is negative.
     */
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount cannot be negative.");
        }
        hp = Math.min(maxHp, hp + amount);
    }

    /**
     * Turns on the shield, which blocks the next hits.
     *
     * @param turns how many hits the shield blocks.
     * @throws IllegalArgumentException if turns is negative.
     */
    public void activateShield(int turns) {
        if (turns < 0) {
            throw new IllegalArgumentException("Shield turns cannot be negative.");
        }
        shieldTurns = turns;
    }

    /**
     * Adds points to the player's score.
     *
     * @param points the number of points to add.
     * @throws IllegalArgumentException if points is negative.
     */
    public void addScore(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative.");
        }
        score += points;
    }

    /** Increases the streak by one. */
    public void increaseStreak() {
        streak++;
    }

    /** Resets the streak to zero. */
    public void resetStreak() {
        streak = 0;
    }
}