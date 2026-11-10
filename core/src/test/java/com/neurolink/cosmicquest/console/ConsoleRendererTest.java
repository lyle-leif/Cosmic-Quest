package com.neurolink.cosmicquest.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.logic.BattleState;
import com.neurolink.cosmicquest.logic.MatchStats;
import com.neurolink.cosmicquest.logic.TurnResult;
import com.neurolink.cosmicquest.model.EnumerationQuestion;
import com.neurolink.cosmicquest.model.IdentificationQuestion;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The renderer prints into memory instead of the screen, so each test can
 * read back exactly what the player would see.
 */
class ConsoleRendererTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final ConsoleRenderer renderer =
            new ConsoleRenderer(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    private String printed() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fullHalfAndEmptyBars() {
        assertEquals("[####################] 100/100", ConsoleRenderer.hpBar(100, 100));
        assertEquals("[##########----------]  50/100", ConsoleRenderer.hpBar(50, 100));
        assertEquals("[--------------------]   0/100", ConsoleRenderer.hpBar(0, 100));
    }

    @Test
    void barAlwaysHasTwentyCells() {
        for (int hp = 0; hp <= 160; hp += 7) {
            String bar = ConsoleRenderer.hpBar(hp, 160);
            assertEquals(ConsoleRenderer.BAR_WIDTH, bar.indexOf(']') - bar.indexOf('[') - 1);
        }
    }

    @Test
    void anyHpAboveZeroShowsAtLeastOneCell() {
        assertTrue(ConsoleRenderer.hpBar(1, 200).startsWith("[#-"));
    }

    @Test
    void barClampsHpOutsideRange() {
        assertEquals(ConsoleRenderer.hpBar(100, 100), ConsoleRenderer.hpBar(150, 100));
        assertEquals(ConsoleRenderer.hpBar(0, 100), ConsoleRenderer.hpBar(-5, 100));
    }

    @Test
    void barRejectsZeroMaxHp() {
        assertThrows(IllegalArgumentException.class, () -> ConsoleRenderer.hpBar(0, 0));
    }

    @Test
    void hudShowsPlayerAlienAndStreak() {
        Player player = new Player("Lyle");
        player.takeDamage(40);
        player.increaseStreak();
        player.increaseStreak();
        player.increaseStreak();
        VoidBrute brute = new VoidBrute(2);

        renderer.showHud(player, brute);

        String text = printed();
        assertTrue(text.contains("Lyle"));
        assertTrue(text.contains("[############--------]  60/100"));
        assertTrue(text.contains("Streak 3 (+30% damage)"));
        assertTrue(text.contains("Void Brute Lv 2"));
        assertTrue(text.contains("[####################] 100/100"));
        assertFalse(text.contains("Shield ON"));
    }

    @Test
    void hudShowsActiveShield() {
        Player player = new Player("Lyle");
        player.activateShield(1);
        renderer.showHud(player, new VoidBrute(1));
        assertTrue(printed().contains("Shield ON"));
    }

    @Test
    void multipleChoiceListsLetteredOptions() {
        renderer.showQuestion(new MultipleChoiceQuestion("A block of code that performs a task.",
                List.of("Class", "Object", "Method", "Field"), 2, 1));
        String text = printed();
        assertTrue(text.contains("[Multiple choice] Difficulty 1/3"));
        assertTrue(text.contains("A) Class"));
        assertTrue(text.contains("D) Field"));
        assertTrue(text.contains("Type A, B, C or D"));
    }

    @Test
    void eachTypeGetsItsOwnInstruction() {
        renderer.showQuestion(new IdentificationQuestion("Stores data.", "Field", 2));
        renderer.showQuestion(new EnumerationQuestion("List the pillars.", List.of("a", "b"), 2));
        String text = printed();
        assertTrue(text.contains("[Identification]"));
        assertTrue(text.contains("Type the term."));
        assertTrue(text.contains("[Enumeration]"));
        assertTrue(text.contains("separated by commas"));
    }

    @Test
    void wrongAnswerShowsTheRightOne() {
        renderer.showAnswerResult(false, new IdentificationQuestion("Stores data.", "Field", 1));
        assertTrue(printed().contains("Wrong. The answer was: Field"));
    }

    @Test
    void correctAnswerSaysCorrect() {
        renderer.showAnswerResult(true, new IdentificationQuestion("Stores data.", "Field", 1));
        assertTrue(printed().contains("Correct!"));
    }

    @Test
    void hitPartialMenuAndErrorLines() {
        renderer.showHit("Lyle", "Void Brute", 22);
        renderer.showPartialMatch(0.75);
        renderer.showMenu("Main menu", List.of("Solo", "Exit"));
        renderer.showError("Line 3: missing ::");
        String text = printed();
        assertTrue(text.contains("Lyle hits Void Brute for 22 damage!"));
        assertTrue(text.contains("You named 75% of the list."));
        assertTrue(text.contains("1) Solo"));
        assertTrue(text.contains("2) Exit"));
        assertTrue(text.contains("! Line 3: missing ::"));
    }

    @Test
    void outputIsPlainAscii() {
        renderer.showTitle();
        renderer.showHud(new Player("Lyle"), new VoidBrute(3));
        assertTrue(printed().chars().allMatch(c -> c < 128), "Only ASCII is safe in the Windows console");
    }

    @Test
    void rejectsNullStream() {
        assertThrows(IllegalArgumentException.class, () -> new ConsoleRenderer(null));
    }

    @Test
    void starRowShowsEarnedAndEmptyStars() {
        assertEquals("[ ] [ ] [ ]", ConsoleRenderer.starRow(0));
        assertEquals("[*] [*] [ ]", ConsoleRenderer.starRow(2));
        assertEquals("[*] [*] [*]", ConsoleRenderer.starRow(3));
    }

    @Test
    void debriefListsTotalsAndMisses() {
        IdentificationQuestion field = new IdentificationQuestion("Stores data.", "Field", 1);
        IdentificationQuestion method = new IdentificationQuestion("A block of code.", "Method", 1);
        MatchStats stats = new MatchStats();
        stats.recordAnswer(new TurnResult(field, "Method", false, false, 0.0, 0, 8, BattleState.ENEMY_ATTACK));
        stats.recordAnswer(new TurnResult(method, null, false, true, 0.0, 0, 8, BattleState.ENEMY_ATTACK));
        stats.recordAnswer(new TurnResult(method, "Method", true, false, 1.0, 22, 0, BattleState.WON));

        renderer.showDebrief(true, 1, 22, stats);

        String out = printed();
        assertTrue(out.contains("MISSION COMPLETE"));
        assertTrue(out.contains("[*] [ ] [ ]"));
        assertTrue(out.contains("Correct 1/3 (33%)"));
        assertTrue(out.contains("Damage dealt 22, taken 16"));
        assertTrue(out.contains("Score 22"));
        assertTrue(out.contains("1. Stores data."));
        assertTrue(out.contains("Your answer:    Method"));
        assertTrue(out.contains("Correct answer: Field"));
        assertTrue(out.contains("2. A block of code."));
        assertTrue(out.contains("Your answer:    (no answer)"));
        assertTrue(out.chars().allMatch(c -> c < 128), "Only ASCII is safe in the Windows console");
    }

    @Test
    void debriefForALossWithNoMisses() {
        renderer.showDebrief(false, 0, 0, new MatchStats());
        String out = printed();
        assertTrue(out.contains("MISSION FAILED"));
        assertTrue(out.contains("[ ] [ ] [ ]"));
        assertTrue(out.contains("No questions missed."));
    }
}
