package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Question;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps score of one battle for the end-of-mission debrief: answers given,
 * damage both ways, and every missed question with what the player typed.
 * It only stores data; the console or GUI decides how to show it.
 */
public class MatchStats {

    /** Most stars a battle can earn. */
    public static final int MAX_STARS = 3;

    private static final int PERCENT = 100;
    private static final int HALF = 2;

    /** One missed question and the player's most recent answer to it. */
    public static final class Miss {
        private final Question question;
        private final String playerAnswer;

        private Miss(Question question, String playerAnswer) {
            this.question = question;
            this.playerAnswer = playerAnswer;
        }

        /** Returns the question that was missed. */
        public Question getQuestion() {
            return question;
        }

        /** Returns what the player typed last time, or "" after a timeout. */
        public String getPlayerAnswer() {
            return playerAnswer;
        }
    }

    private int correctAnswers;
    private int totalAnswers;
    private int damageDealt;
    private int damageTaken;
    private final Map<Question, String> missed = new LinkedHashMap<>();

    /**
     * Records one answered turn.
     *
     * @param result what happened in the turn.
     * @throws IllegalArgumentException if result is null.
     */
    public void recordAnswer(TurnResult result) {
        if (result == null) {
            throw new IllegalArgumentException("The turn result must not be null.");
        }
        totalAnswers++;
        damageDealt += result.getDamageDealt();
        damageTaken += result.getDamageTaken();
        if (result.isCorrect()) {
            correctAnswers++;
        } else {
            // put() on a key already present keeps its place and updates the answer.
            missed.put(result.getQuestion(), result.getPlayerAnswer());
        }
    }

    /**
     * Works out the stars for a finished battle:
     * 0 for a loss, 1 for a win, 2 if the hull is also above 50%, and 3 if
     * nothing was missed as well.
     *
     * @param won true if the battle was won.
     * @param hullLeft the hull at the end.
     * @param maxHull the full hull, above 0.
     * @return 0 to 3.
     * @throws IllegalArgumentException if maxHull is 0 or less.
     */
    public int getStars(boolean won, int hullLeft, int maxHull) {
        if (maxHull <= 0) {
            throw new IllegalArgumentException("Max hull must be above 0, got " + maxHull + ".");
        }
        if (!won) {
            return 0;
        }
        int stars = 1;
        if (hullLeft * HALF > maxHull) {
            stars++;
            if (missed.isEmpty()) {
                stars++;
            }
        }
        return stars;
    }

    /** Returns the share of answers that were right, as a whole percent; 0 before any answer. */
    public int getAccuracyPercent() {
        if (totalAnswers == 0) {
            return 0;
        }
        return (int) Math.round((double) correctAnswers * PERCENT / totalAnswers);
    }

    /**
     * Returns every missed question once, in the order first missed, with
     * the player's most recent answer. The list cannot be changed.
     *
     * @return the misses for the debrief.
     */
    public List<Miss> getMisses() {
        List<Miss> misses = new ArrayList<>();
        for (Map.Entry<Question, String> entry : missed.entrySet()) {
            misses.add(new Miss(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(misses);
    }

    /** Returns how many answers were fully right. */
    public int getCorrectAnswers() {
        return correctAnswers;
    }

    /** Returns how many answers were given, including timeouts. */
    public int getTotalAnswers() {
        return totalAnswers;
    }

    /** Returns the total damage the player dealt. */
    public int getDamageDealt() {
        return damageDealt;
    }

    /** Returns the total damage the player took. */
    public int getDamageTaken() {
        return damageTaken;
    }
}
