package com.neurolink.cosmicquest.model;

/**
 * A beast that asks you to list every item in a group.
 */
public class LunarBeast extends Entity {

    private static final int[] HP = {80, 130, 200};
    private static final int[] ATTACK = {11, 15, 22};

    /**
     * Creates a Lunar Beast at the given level, with full hull.
     *
     * @param level the threat level, from 1 to 3.
     * @throws IllegalArgumentException if the level is not 1, 2 or 3.
     */
    public LunarBeast(int level) {
        super("Lunar Beast", checkLevel(level), HP[level - 1], HP[level - 1], ATTACK[level - 1]);
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
        return QuestionType.ENUMERATION;
    }

    /** Returns a description of this alien's special move. */
    @Override
    public String getSpecialMove() {
        return "Moon Pounce: damage grows with each item missed.";
    }
}