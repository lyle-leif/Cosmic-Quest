package com.neurolink.cosmicquest.model;

/**
 * A question that shows a definition and asks the player to type the term.
 * Spaces at the edges, extra spaces and capital letters do not matter.
 */
public class IdentificationQuestion extends Question {

    /**
     * Creates an identification question.
     *
     * @param questionText the definition shown to the player.
     * @param correctAnswer the term the player must type.
     * @param difficulty from 1 (easy) to 3 (hard).
     */
    public IdentificationQuestion(String questionText, String correctAnswer, int difficulty) {
        super(questionText, correctAnswer, difficulty);
    }

    /**
     * Correct when the typed text matches the term after both are normalized.
     *
     * @param input what the player typed.
     * @return true if it matches the term.
     */
    @Override
    public boolean checkAnswer(String input) {
        return normalize(input).equals(normalize(getCorrectAnswer()));
    }

    /** Returns IDENTIFICATION. */
    @Override
    public QuestionType getType() {
        return QuestionType.IDENTIFICATION;
    }
}