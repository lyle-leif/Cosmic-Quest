package com.neurolink.cosmicquest.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A question that asks the player to list every item of a group, separated
 * by commas. Order, capital letters, extra spaces and punctuation do not
 * matter. A partly right answer still earns a match ratio, which the battle
 * uses for partial damage.
 */
public class EnumerationQuestion extends Question {

    private static final String ITEM_SEPARATOR = ",";
    private static final String JOINER = ", ";
    private static final String NOT_LETTER_OR_DIGIT = "[^a-z0-9 ]";
    private static final double FULL_MATCH = 1.0;

    private final List<String> items;

    /**
     * Creates an enumeration question.
     *
     * @param questionText what the player is asked, for example "List the four OOP pillars."
     * @param items every item the player must name.
     * @param difficulty from 1 (easy) to 3 (hard).
     * @throws IllegalArgumentException if the list is null, empty or has a blank item.
     */
    public EnumerationQuestion(String questionText, List<String> items, int difficulty) {
        super(questionText, joinItems(items), difficulty);
        this.items = List.copyOf(items);
    }

    /**
     * Checks the items before the constructor uses them, and joins them so
     * they can be passed to the parent as the correct answer.
     *
     * @param items the items to check.
     * @return the items joined with ", ".
     * @throws IllegalArgumentException if the list is null, empty or has a blank item.
     */
    private static String joinItems(List<String> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An enumeration question needs at least one item.");
        }
        for (String item : items) {
            if (item == null || clean(item).isEmpty()) {
                throw new IllegalArgumentException("Items must not be blank.");
            }
        }
        return String.join(JOINER, items);
    }

    /** Returns every item the player must name. The list cannot be changed. */
    public List<String> getItems() {
        return items;
    }

    /**
     * Works out how much of the list the player named. Each item counts once,
     * even if the player repeats it.
     *
     * @param input what the player typed, items separated by commas.
     * @return from 0.0 (none named) to 1.0 (all named); 3 of 4 gives 0.75.
     */
    public double getMatchRatio(String input) {
        if (input == null || input.isBlank()) {
            return 0.0;
        }
        Set<String> answered = new HashSet<>();
        for (String part : input.split(ITEM_SEPARATOR)) {
            answered.add(clean(part));
        }
        int matched = 0;
        for (String item : items) {
            if (answered.contains(clean(item))) {
                matched++;
            }
        }
        return (double) matched / items.size();
    }

    /**
     * Correct only when every item is named.
     *
     * @param input what the player typed, items separated by commas.
     * @return true if the match ratio is 1.0.
     */
    @Override
    public boolean checkAnswer(String input) {
        return getMatchRatio(input) >= FULL_MATCH;
    }

    /** Returns ENUMERATION. */
    @Override
    public QuestionType getType() {
        return QuestionType.ENUMERATION;
    }

    /**
     * Normalizes the text and removes punctuation, so "Objects." matches "objects".
     *
     * @param s the text to clean up.
     * @return the cleaned-up text.
     */
    private static String clean(String s) {
        return normalize(normalize(s).replaceAll(NOT_LETTER_OR_DIGIT, " "));
    }
}
