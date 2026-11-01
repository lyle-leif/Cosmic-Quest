package com.neurolink.cosmicquest.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A question with four options, A to D. The player can answer with the
 * letter or with the option's text.
 */
public class MultipleChoiceQuestion extends Question {

    private static final int OPTION_COUNT = 4;
    private static final char FIRST_LETTER = 'a';

    private final List<String> options;
    private final int correctIndex;

    /**
     * Creates a multiple choice question.
     *
     * @param questionText what the player is asked.
     * @param options exactly four options, shown as A to D.
     * @param correctIndex the position of the right option, from 0 (A) to 3 (D).
     * @param difficulty from 1 (easy) to 3 (hard).
     * @throws IllegalArgumentException if there are not four non-blank options
     *         or the index is not 0 to 3.
     */
    public MultipleChoiceQuestion(String questionText, List<String> options, int correctIndex, int difficulty) {
        super(questionText, pickCorrectOption(options, correctIndex), difficulty);
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
        this.correctIndex = correctIndex;
    }

    /**
     * Checks the options before the constructor uses them, and returns the
     * right one so it can be passed to the parent as the correct answer.
     *
     * @param options the options to check.
     * @param correctIndex the position of the right option.
     * @return the text of the right option.
     * @throws IllegalArgumentException if the options or the index are not valid.
     */
    private static String pickCorrectOption(List<String> options, int correctIndex) {
        if (options == null || options.size() != OPTION_COUNT) {
            throw new IllegalArgumentException("A multiple choice question needs exactly 4 options.");
        }
        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new IllegalArgumentException("Options must not be blank.");
            }
        }
        if (correctIndex < 0 || correctIndex >= OPTION_COUNT) {
            throw new IllegalArgumentException("The correct index must be 0 to 3, got " + correctIndex + ".");
        }
        return options.get(correctIndex);
    }

    /** Returns the four options in A to D order. The list cannot be changed. */
    public List<String> getOptions() {
        return options;
    }

    /** Returns the position of the right option, from 0 (A) to 3 (D). */
    public int getCorrectIndex() {
        return correctIndex;
    }

    /**
     * Correct when the player gives the right letter (A to D, any case) or
     * the right option's text.
     *
     * @param input what the player typed.
     * @return true if it names the right option.
     */
    @Override
    public boolean checkAnswer(String input) {
        String answer = normalize(input);
        if (answer.length() == 1) {
            int index = answer.charAt(0) - FIRST_LETTER;
            if (index >= 0 && index < OPTION_COUNT) {
                return index == correctIndex;
            }
        }
        return answer.equals(normalize(getCorrectAnswer()));
    }

    /** Returns MULTIPLE_CHOICE. */
    @Override
    public QuestionType getType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}