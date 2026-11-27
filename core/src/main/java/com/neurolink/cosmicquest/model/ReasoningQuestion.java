package com.neurolink.cosmicquest.model;

import java.util.List;

/**
 * A question that asks the player to explain a term in their own words. The
 * answer passes when it uses enough of the term's keywords. Keywords must
 * appear as whole words, so "class" is not found inside "subclass".
 */
public class ReasoningQuestion extends Question {

    /** The share of keywords an answer must use to pass: 3 of 5 passes. */
    public static final double KEYWORD_PASS_RATIO = 0.6;

    private static final String NOT_LETTER_OR_DIGIT = "[^a-z0-9 ]";
    private static final String SPACE = " ";

    private final List<String> keywords;

    /**
     * Creates a reasoning question.
     *
     * @param questionText what the player is asked, for example "Explain Class in your own words."
     * @param correctAnswer the term's definition, shown in the debrief.
     * @param keywords the words a good answer should use.
     * @param difficulty from 1 (easy) to 3 (hard).
     * @throws IllegalArgumentException if the list is null, empty or has a blank keyword.
     */
    public ReasoningQuestion(String questionText, String correctAnswer, List<String> keywords, int difficulty) {
        super(questionText, correctAnswer, checkKeywords(keywords, difficulty));
        this.keywords = List.copyOf(keywords);
    }

    /**
     * Checks the keywords before the constructor stores them.
     *
     * @param keywords the keywords to check.
     * @param difficulty the difficulty, passed through unchanged.
     * @return the same difficulty, so this check can run inside {@code super(...)}.
     * @throws IllegalArgumentException if the list is null, empty or has a blank keyword.
     */
    private static int checkKeywords(List<String> keywords, int difficulty) {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("A reasoning question needs at least one keyword.");
        }
        for (String keyword : keywords) {
            if (keyword == null || clean(keyword).isEmpty()) {
                throw new IllegalArgumentException("Keywords must not be blank.");
            }
        }
        return difficulty;
    }

    /** Returns the words a good answer should use. The list cannot be changed. */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Works out how many keywords the answer uses, as whole words.
     *
     * @param input what the player typed.
     * @return from 0.0 (none used) to 1.0 (all used).
     */
    public double getKeywordRatio(String input) {
        if (input == null || input.isBlank()) {
            return 0.0;
        }
        String answer = SPACE + clean(input) + SPACE;
        int matched = 0;
        for (String keyword : keywords) {
            if (answer.contains(SPACE + clean(keyword) + SPACE)) {
                matched++;
            }
        }
        return (double) matched / keywords.size();
    }

    /**
     * Correct when the answer uses at least {@link #KEYWORD_PASS_RATIO} of the keywords.
     *
     * @param input what the player typed.
     * @return true if enough keywords appear.
     */
    @Override
    public boolean checkAnswer(String input) {
        return getKeywordRatio(input) >= KEYWORD_PASS_RATIO;
    }

    /** Returns REASONING. */
    @Override
    public QuestionType getType() {
        return QuestionType.REASONING;
    }

    /**
     * Normalizes the text and turns punctuation into spaces, so "objects."
     * matches "objects".
     *
     * @param s the text to clean up.
     * @return the cleaned-up text.
     */
    private static String clean(String s) {
        return normalize(normalize(s).replaceAll(NOT_LETTER_OR_DIGIT, SPACE));
    }
}
