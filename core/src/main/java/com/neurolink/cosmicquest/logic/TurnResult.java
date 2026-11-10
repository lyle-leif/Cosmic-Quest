package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Question;

/**
 * What happened in one turn. The battle logic returns it instead of printing,
 * and the console or GUI decides how to show it. A TurnResult cannot be
 * changed after it is created.
 */
public final class TurnResult {

    private final Question question;
    private final String playerAnswer;
    private final boolean correct;
    private final boolean timedOut;
    private final double matchRatio;
    private final int damageDealt;
    private final int damageTaken;
    private final BattleState newState;

    /**
     * Records one turn.
     *
     * @param question the question that was asked.
     * @param playerAnswer what the player typed; null is stored as "" (for a timeout).
     * @param correct true if the answer was fully right.
     * @param timedOut true if the player ran out of time.
     * @param matchRatio how much of the answer was right, from 0.0 to 1.0
     *        (partial values only for enumeration).
     * @param damageDealt damage the player dealt to the alien.
     * @param damageTaken damage the player took.
     * @param newState the battle state after this turn.
     * @throws IllegalArgumentException if question or newState is null, a damage
     *         is negative, or the ratio is outside 0.0 to 1.0.
     */
    public TurnResult(Question question, String playerAnswer, boolean correct, boolean timedOut,
            double matchRatio, int damageDealt, int damageTaken, BattleState newState) {
        if (question == null || newState == null) {
            throw new IllegalArgumentException("Question and new state must not be null.");
        }
        if (damageDealt < 0 || damageTaken < 0) {
            throw new IllegalArgumentException("Damage must not be negative.");
        }
        if (Double.isNaN(matchRatio) || matchRatio < 0.0 || matchRatio > 1.0) {
            throw new IllegalArgumentException("Match ratio must be 0.0 to 1.0, got " + matchRatio + ".");
        }
        this.question = question;
        this.playerAnswer = playerAnswer == null ? "" : playerAnswer;
        this.correct = correct;
        this.timedOut = timedOut;
        this.matchRatio = matchRatio;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.newState = newState;
    }

    /** Returns the question that was asked. */
    public Question getQuestion() {
        return question;
    }

    /** Returns what the player typed, or "" after a timeout. */
    public String getPlayerAnswer() {
        return playerAnswer;
    }

    /** Returns true if the answer was fully right. */
    public boolean isCorrect() {
        return correct;
    }

    /** Returns true if the player ran out of time. */
    public boolean isTimedOut() {
        return timedOut;
    }

    /** Returns how much of the answer was right, from 0.0 to 1.0. */
    public double getMatchRatio() {
        return matchRatio;
    }

    /** Returns the damage the player dealt to the alien. */
    public int getDamageDealt() {
        return damageDealt;
    }

    /** Returns the damage the player took. */
    public int getDamageTaken() {
        return damageTaken;
    }

    /** Returns the battle state after this turn. */
    public BattleState getNewState() {
        return newState;
    }
}
