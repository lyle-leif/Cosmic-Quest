package com.neurolink.cosmicquest.model;

/**
 * A lich that asks multiple choice questions.
 */
public class NebulaLich extends Entity {

    private static final int[] HP = {70, 120, 180};
    private static final int[] ATTACK = {9, 13, 19};

    /**
     * Creates a Nebula Lich at the given level, with full hull.
     *
     * @param level the threat level, from 1 to 3.
     * @throws IllegalArgumentException if the level is not 1, 2 or 3.
     */
    public NebulaLich(int level) {
        super("Nebula Lich", checkLevel(level), HP[level - 1], HP[level - 1], ATTACK[level - 1]);
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
        return QuestionType.MULTIPLE_CHOICE;
    }

    /** Returns a description of this alien's special move. */
    @Override
    public String getSpecialMove() {
        return "Hex Bolt: reshuffles the choices at level 3.";
    }
}