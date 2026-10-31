package com.neurolink.cosmicquest.model;

/**
 * A brute that asks you to name the term from its definition.
 */
public class VoidBrute extends Entity {

    private static final int[] HP = {60, 100, 160};
    private static final int[] ATTACK = {8, 12, 18};

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

    /** Returns a description of this alien's special move. */
    @Override
    public String getSpecialMove() {
        return "Ground Slam: a heavy hit every 3rd turn.";
    }
}