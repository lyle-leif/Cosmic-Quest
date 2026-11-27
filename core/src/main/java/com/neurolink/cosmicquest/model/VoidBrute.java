package com.neurolink.cosmicquest.model;

/**
 * A brute that asks you to name the term from its definition.
 */
public class VoidBrute extends Entity {

    private static final int[] HP = {60, 100, 160};
    private static final int[] ATTACK = {8, 12, 18};
    private static final int GROUND_SLAM_EVERY = 3;
    private static final double GROUND_SLAM_MULTIPLIER = 1.5;

    /**
     * Creates a Void Brute at the given level, with full hull.
     *
     * @param level the threat level, from 1 to 3.
     * @throws IllegalArgumentException if the level is not 1, 2 or 3.
     */
    public VoidBrute(int level) {
        super("Void Brute", checkLevel(level), HP[level - 1], HP[level - 1], ATTACK[level - 1]);
    }

    /**
     * Checks the level before the constructor uses it as an array index.
     *
     * @param level the level to check.
     * @return the same level, if it is valid.
     * @throws IllegalArgumentException if the level is not 1, 2 or 3.
     */
    private static int checkLevel(int level) {
        if (level < 1 || level > 3) {
            throw new IllegalArgumentException("Level must be 1 to 3, got " + level + ".");
        }
        return level;
    }

    /** Returns the type of question this alien asks. */
    @Override
    public QuestionType getQuestionType() {
        return QuestionType.IDENTIFICATION;
    }

    /**
     * Ground Slam: every 3rd turn the strike deals 1.5 times the attack power.
     *
     * @param turn the battle's turn number, from 1.
     * @param itemsMissed not used by this alien.
     * @return the damage of this strike.
     */
    @Override
    public int getStrikeDamage(int turn, int itemsMissed) {
        int damage = super.getStrikeDamage(turn, itemsMissed);
        if (turn > 0 && turn % GROUND_SLAM_EVERY == 0) {
            return (int) Math.round(damage * GROUND_SLAM_MULTIPLIER);
        }
        return damage;
    }

    /** Returns a description of this alien's special move. */
    @Override
    public String getSpecialMove() {
        return "Ground Slam: a heavy hit every 3rd turn.";
    }
}