package com.neurolink.cosmicquest.model;

/**
 * Represents the hero controlled by the person playing, tracking hull, score
 * and streak.
 */
public class Player implements Combatant {

    private static final int STARTING_HP = 100;

    private String name;
    private int hp;
    private int maxHp;
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

    /** Gets the value of private variable hp.*/
    @Override
    public int getHp() {
        return hp;
    }

    /** Gets the value of private variable maxHp.*/
    @Override
    public int getMaxHp() {
        return maxHp;
    }

    /** It returns true when hp is zero.*/
    @Override
    public boolean isDefeated() {
        return hp == 0;
    }

    /**
     * The shield blocks the hit, otherwise hp goes down but never below 0.
     *
     * @param amount The damage the hit would deal.
     */
    @Override
    public void takeDamage(int amount) {
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
     */
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    /**
     * Adds points to the player's score.
     *
     * @param points the number of points to add.
     */
    public void addScore(int points) {
        score += points;
    }

    /** Increases the streak. */
    public void increaseStreak() {
        streak++;
    }

    /** Resets the streak. */
    public void resetStreak() {
        streak = 0;
    }
}