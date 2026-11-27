package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Question;
import com.neurolink.cosmicquest.model.ReasoningQuestion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helps with the current question without changing it:
 * <ul>
 * <li>Multiple choice: hides 2 wrong options.</li>
 * <li>Identification: shows the first letter of the term.</li>
 * <li>Enumeration: shows the first letter of every item.</li>
 * <li>Reasoning: suggests one keyword.</li>
 * </ul>
 * The hint is stored in the battle and cleared when the next question starts.
 */
public class Hint extends PowerUp {

    private static final int OPTIONS_TO_HIDE = 2;
    private static final char HIDDEN_LETTER = '_';
    private static final char FIRST_LETTER = 'A';

    /** Creates a Hint. */
    public Hint() {
        super("Hint", "Helps with the current question.");
    }

    /** Works out the hint for the current question and stores it in the battle. */
    @Override
    public void apply(Player player, BattleMode battle) {
        Question question = battle.getCurrentQuestion();
        if (question instanceof MultipleChoiceQuestion choice) {
            Set<Integer> hidden = pickWrongOptions(choice, battle);
            battle.setHint("Not " + letters(hidden) + ".", hidden);
        } else if (question instanceof EnumerationQuestion enumeration) {
            battle.setHint("Starts with: " + firstLetters(enumeration.getItems()), Set.of());
        } else if (question instanceof ReasoningQuestion reasoning) {
            battle.setHint("Try using the word \"" + reasoning.getKeywords().get(0) + "\".", Set.of());
        } else {
            battle.setHint(maskAfterFirstLetter(question.getCorrectAnswer()), Set.of());
        }
    }

    /**
     * Hides every letter except the first, keeping spaces, so "Abstract class"
     * becomes "A_______ _____".
     *
     * @param answer the answer to mask.
     * @return the masked answer.
     */
    static String maskAfterFirstLetter(String answer) {
        StringBuilder masked = new StringBuilder(answer.length());
        for (int i = 0; i < answer.length(); i++) {
            char c = answer.charAt(i);
            masked.append(i == 0 || c == ' ' ? c : HIDDEN_LETTER);
        }
        return masked.toString();
    }

    /** Picks 2 wrong options at random, using the battle's randomness. */
    private static Set<Integer> pickWrongOptions(MultipleChoiceQuestion choice, BattleMode battle) {
        List<Integer> wrong = new ArrayList<>();
        for (int i = 0; i < choice.getOptions().size(); i++) {
            if (i != choice.getCorrectIndex()) {
                wrong.add(i);
            }
        }
        Collections.shuffle(wrong, battle.random);
        return new TreeSet<>(wrong.subList(0, OPTIONS_TO_HIDE));
    }

    /** Turns option positions into letters, such as "B or D". */
    private static String letters(Set<Integer> indexes) {
        List<String> letters = new ArrayList<>();
        for (int index : indexes) {
            letters.add(String.valueOf((char) (FIRST_LETTER + index)));
        }
        return String.join(" or ", letters);
    }

    /** Shows each item's first letter, such as "E..., I..., P...". */
    private static String firstLetters(List<String> items) {
        List<String> starts = new ArrayList<>();
        for (String item : items) {
            starts.add(item.charAt(0) + "...");
        }
        return String.join(", ", starts);
    }
}
