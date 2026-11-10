package com.neurolink.cosmicquest.console;

import com.neurolink.cosmicquest.logic.DamageCalculator;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Question;
import java.io.PrintStream;
import java.util.List;

/**
 * Prints everything the player sees in the console version: the HUD,
 * questions, results, menus and messages. It is the only class in the game
 * that prints. The rest of the game returns data, and this class turns it
 * into text.
 *
 * <p>Only plain ASCII is used, because the Windows console can show symbols
 * such as block characters or emojis as "?".
 */
public class ConsoleRenderer {

    /** Number of characters inside an HP bar. */
    public static final int BAR_WIDTH = 20;

    private static final char BAR_FULL = '#';
    private static final char BAR_EMPTY = '-';
    private static final char FIRST_LETTER = 'A';
    private static final int NAME_WIDTH = 18;
    private static final String DIVIDER = "-".repeat(56);
    private static final String TITLE_LINE = "=".repeat(56);
    private static final String PROMPT = "> ";
    private static final String ERROR_PREFIX = "! ";

    private final PrintStream out;

    /** Creates a renderer that prints to the screen. */
    public ConsoleRenderer() {
        this(System.out);
    }

    /**
     * Creates a renderer that prints to the given stream. Tests pass a stream
     * that writes to memory so they can check the printed text.
     *
     * @param out where to print.
     * @throws IllegalArgumentException if out is null.
     */
    public ConsoleRenderer(PrintStream out) {
        if (out == null) {
            throw new IllegalArgumentException("The output stream must not be null.");
        }
        this.out = out;
    }

    /**
     * Builds an HP bar such as {@code [##########----------]  50/100}. Any HP
     * above 0 shows at least one #, so a nearly defeated fighter does not
     * look already defeated.
     *
     * @param hp the current HP; values outside 0 to maxHp are clamped.
     * @param maxHp the full HP, above 0.
     * @return the bar text.
     * @throws IllegalArgumentException if maxHp is 0 or less.
     */
    public static String hpBar(int hp, int maxHp) {
        if (maxHp <= 0) {
            throw new IllegalArgumentException("Max HP must be above 0, got " + maxHp + ".");
        }
        int shown = Math.max(0, Math.min(hp, maxHp));
        int filled = (int) Math.round((double) shown * BAR_WIDTH / maxHp);
        if (shown > 0 && filled == 0) {
            filled = 1;
        }
        String bar = String.valueOf(BAR_FULL).repeat(filled)
                + String.valueOf(BAR_EMPTY).repeat(BAR_WIDTH - filled);
        return String.format("[%s] %3d/%d", bar, shown, maxHp);
    }

    /**
     * Returns the letter for an option position: 0 is A, 3 is D.
     *
     * @param index the option position, from 0.
     * @return the letter.
     */
    public static char optionLetter(int index) {
        return (char) (FIRST_LETTER + index);
    }

    /** Prints the game title. */
    public void showTitle() {
        out.println(TITLE_LINE);
        out.println("                 C O S M I C   Q U E S T");
        out.println(TITLE_LINE);
    }

    /**
     * Prints the battle HUD: the player's hull, streak and shield, then the
     * alien's name, level and hull.
     *
     * @param player the player.
     * @param entity the alien being fought.
     */
    public void showHud(Player player, Entity entity) {
        out.println(DIVIDER);
        out.printf(" %-" + NAME_WIDTH + "s Hull %s%n", player.getName(),
                hpBar(player.getHp(), player.getMaxHp()));
        out.printf(" %-" + NAME_WIDTH + "s Streak %d (+%d%% damage)%s%n", "",
                player.getStreak(), DamageCalculator.streakBonusPercent(player.getStreak()),
                player.getShieldTurns() > 0 ? "  Shield ON" : "");
        String alien = entity.getName() + " Lv " + entity.getLevel();
        out.printf(" %-" + NAME_WIDTH + "s Hull %s%n", alien,
                hpBar(entity.getHp(), entity.getMaxHp()));
        out.println(DIVIDER);
    }

    /**
     * Prints a question with an instruction that fits its type. Multiple
     * choice questions also list their options as A) to D).
     *
     * @param question the question to show.
     */
    public void showQuestion(Question question) {
        out.println();
        out.printf("[%s] Difficulty %d/3%n", typeLabel(question), question.getDifficulty());
        out.println(question.getQuestionText());
        if (question instanceof MultipleChoiceQuestion choice) {
            List<String> options = choice.getOptions();
            for (int i = 0; i < options.size(); i++) {
                out.printf("  %c) %s%n", optionLetter(i), options.get(i));
            }
        }
        out.println("(" + instruction(question) + ")");
    }

    /** Prints the input prompt, without a line break, so the answer is typed after it. */
    public void showPrompt() {
        out.print(PROMPT);
        out.flush();
    }

    /**
     * Prints whether the answer was right. A wrong answer also shows the
     * correct one, so the player learns it.
     *
     * @param correct true if the answer was right.
     * @param question the question that was answered.
     */
    public void showAnswerResult(boolean correct, Question question) {
        if (correct) {
            out.println("Correct!");
        } else {
            out.println("Wrong. The answer was: " + question.getCorrectAnswer());
        }
    }

    /**
     * Prints how much of an enumeration list the player named.
     *
     * @param ratio the share of items named, from 0.0 to 1.0.
     */
    public void showPartialMatch(double ratio) {
        out.printf("You named %d%% of the list.%n", Math.round(ratio * 100));
    }

    /**
     * Prints one hit, such as "Lyle hits Void Brute for 22 damage!".
     *
     * @param attacker who attacks.
     * @param target who is hit.
     * @param damage how much damage was dealt.
     */
    public void showHit(String attacker, String target, int damage) {
        out.printf("%s hits %s for %d damage!%n", attacker, target, damage);
    }

    /**
     * Prints a numbered menu, starting at 1.
     *
     * @param title the menu heading.
     * @param options the choices, in order.
     */
    public void showMenu(String title, List<String> options) {
        out.println();
        out.println(title);
        for (int i = 0; i < options.size(); i++) {
            out.printf("  %d) %s%n", i + 1, options.get(i));
        }
    }

    /**
     * Prints a plain line of text.
     *
     * @param message the text to print.
     */
    public void showMessage(String message) {
        out.println(message);
    }

    /**
     * Prints a problem the player should fix, such as a bad menu choice or a
     * lesson file error.
     *
     * @param message what went wrong.
     */
    public void showError(String message) {
        out.println(ERROR_PREFIX + message);
    }

    /** Returns a short, readable name for the question's type. */
    private static String typeLabel(Question question) {
        switch (question.getType()) {
            case IDENTIFICATION:
                return "Identification";
            case MULTIPLE_CHOICE:
                return "Multiple choice";
            case ENUMERATION:
                return "Enumeration";
            case REASONING:
                return "Reasoning";
            default:
                return question.getType().toString();
        }
    }

    /** Returns how the player should answer this type of question. */
    private static String instruction(Question question) {
        switch (question.getType()) {
            case IDENTIFICATION:
                return "Type the term.";
            case MULTIPLE_CHOICE:
                return "Type A, B, C or D, or the answer.";
            case ENUMERATION:
                return "List every item, separated by commas.";
            case REASONING:
                return "Explain in your own words.";
            default:
                return "Type your answer.";
        }
    }
}
