package com.neurolink.cosmicquest.console;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Plays the console game from scripted input. Each test types its lines in
 * advance, then reads back everything the game printed.
 */
class ConsoleMainTest {

    private static final long SECOND = 1_000_000_000L;

    /** One term only, so every identification answer is "Class". */
    private static final String ONE_TERM_LESSON =
            "Class :: A blueprint that describes the fields and methods of an object.\n";

    @TempDir
    Path folder;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /** Runs the game with this input and a clock that moves the given step per reading. */
    private String play(String input, long clockStep) {
        long[] now = {0};
        LongSupplier clock = () -> now[0] += clockStep;
        ConsoleRenderer renderer = new ConsoleRenderer(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        new ConsoleMain(new Scanner(input), renderer, new Random(42), clock).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private String play(String input) {
        return play(input, SECOND);
    }

    private String lesson(String text) throws IOException {
        Path file = folder.resolve("lesson.txt");
        Files.writeString(file, text);
        return file.toString();
    }

    /** Menu 3 with the lesson path, then Solo against a level 1 Void Brute as "Pilot". */
    private String soloVsVoidBrute(String text) throws IOException {
        return "3\n" + lesson(text) + "\n1\n\n1\n1\n";
    }

    @Test
    void exitLeavesTheGame() {
        assertTrue(play("5\n").contains("Goodbye"));
    }

    @Test
    void lettersBlankLinesAndOutOfRangeNumbersNeverCrash() {
        String out = play("abc\n\n0\n9\n-1\n5\n");
        assertTrue(out.split("Type a number from 1 to 5", -1).length - 1 == 5);
        assertTrue(out.contains("Goodbye"));
    }

    @Test
    void closedInputEndsCleanly() {
        assertTrue(play("").contains("Goodbye"), "Ctrl+Z / Ctrl+D must not crash");
    }

    @Test
    void closedInputInTheMiddleOfABattleEndsCleanly() throws IOException {
        assertTrue(play(soloVsVoidBrute(ONE_TERM_LESSON)).contains("Goodbye"));
    }

    @Test
    void soloNeedsALesson() {
        assertTrue(play("1\n5\n").contains("Load a lesson first"));
    }

    @Test
    void missingLessonFileIsReported() {
        String missing = folder.resolve("nope.txt").toString();
        String out = play("3\n" + missing + "\n5\n");
        assertTrue(out.contains("No file found"));
        assertTrue(out.contains("Goodbye"));
    }

    @Test
    void badLessonShowsTheLineNumber() throws IOException {
        String path = lesson("Class :: A blueprint.\nthis line has no separator\n");
        assertTrue(play("3\n" + path + "\n5\n").contains("Line 2"));
    }

    @Test
    void quotedPathFromCopyAsPathLoads() throws IOException {
        String path = lesson(ONE_TERM_LESSON);
        assertTrue(play("3\n\"" + path + "\"\n5\n").contains("Loaded lesson: lesson.txt"));
    }

    @Test
    void soloBattlePlaysFromMenuToDebrief() throws IOException {
        // Void Brute Lv 1 has 60 hull. Identification is difficulty 2 (x1.25):
        // 28 + 30 + 33 damage wins on the 3rd answer.
        String out = play(soloVsVoidBrute(ONE_TERM_LESSON) + "\nClass\nClass\nClass\n\n5\n");
        assertTrue(out.contains("Type an answer"), "A blank answer is asked again, not counted");
        assertTrue(out.contains("Pilot hits Void Brute for 28 damage!"));
        assertTrue(out.contains("MISSION COMPLETE"));
        assertTrue(out.contains("[*] [*] [*]"));
        assertTrue(out.contains("Correct 3/3"));
        assertTrue(out.contains("Goodbye"));
    }

    @Test
    void wrongAnswerLetsTheAlienStrike() throws IOException {
        String out = play(soloVsVoidBrute(ONE_TERM_LESSON) + "Object\n/quit\n\n5\n");
        assertTrue(out.contains("Wrong. The answer was: Class"));
        assertTrue(out.contains("Void Brute hits Pilot for 8 damage!"));
    }

    @Test
    void quittingEndsTheBattleAsALoss() throws IOException {
        String out = play(soloVsVoidBrute(ONE_TERM_LESSON) + "/quit\n\n5\n");
        assertTrue(out.contains("You retreat"));
        assertTrue(out.contains("MISSION FAILED"));
        assertTrue(out.contains("Goodbye"));
    }

    @Test
    void lateAnswerCountsAsTimeoutEvenIfRight() throws IOException {
        // The clock moves 31 seconds between showing the question and reading the answer.
        String out = play(soloVsVoidBrute(ONE_TERM_LESSON) + "Class\n/quit\n\n5\n", 31 * SECOND);
        assertTrue(out.contains("Time's up"));
        assertTrue(out.contains("Correct 0/1"));
    }

    @Test
    void badBattleCommandsNeverCrash() throws IOException {
        String out = play(soloVsVoidBrute(ONE_TERM_LESSON) + "/1\n/x\n/help\n/quit\n\n5\n");
        assertTrue(out.contains("There is no power-up in slot 1"));
        assertTrue(out.contains("Unknown command /x"));
        assertTrue(out.contains("HOW TO PLAY"));
        assertTrue(out.contains("MISSION FAILED"));
    }

    @Test
    void onlyAliensTheLessonCanQuizAreListed() throws IOException {
        String out = play("3\n" + lesson(ONE_TERM_LESSON) + "\n1\n\n");
        assertTrue(out.contains("Void Brute"));
        assertFalse(out.contains("Lunar Beast"), "No [ENUM] lists, so no Lunar Beast");
        assertFalse(out.contains("Nebula Lich"), "Fewer than 4 terms, so no Nebula Lich");
    }

    @Test
    void defaultLessonIsFoundFromCoreFolder() {
        ConsoleMain game = new ConsoleMain(new Scanner(""),
                new ConsoleRenderer(new PrintStream(buffer, true, StandardCharsets.UTF_8)),
                new Random(42), () -> 0);
        assertTrue(game.loadDefaultLesson());
    }

    @Test
    void outputIsPlainAscii() throws IOException {
        String out = play(soloVsVoidBrute(ONE_TERM_LESSON) + "/help\nClass\nObject\n/quit\n\n4\n5\n");
        assertTrue(out.chars().allMatch(c -> c < 128), "Only ASCII is safe in the Windows console");
    }

    @Test
    void rejectsNullArguments() {
        ConsoleRenderer renderer = new ConsoleRenderer(new PrintStream(buffer));
        assertThrows(IllegalArgumentException.class,
                () -> new ConsoleMain(null, renderer, new Random(), () -> 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ConsoleMain(new Scanner(""), renderer, new Random(), null));
    }

    /** Menu 3 with the lesson path, then Squad: 2 pilots with default names, against a Void Brute. */
    private String squadVsVoidBrute(String text) throws IOException {
        return "3\n" + lesson(text) + "\n2\n1\n\n\n1\n";
    }

    @Test
    void squadBattleTakesTurnsAndWins() throws IOException {
        // Level 3 Void Brute has 160 hull: 28 + 28 + 30 + 30 + 33 + 33 wins on the 6th answer.
        String out = play(squadVsVoidBrute(ONE_TERM_LESSON) + "Class\n".repeat(6) + "\n5\n");
        assertTrue(out.contains("Void Brute Lv 3 appears!"), "Squad always fights a level 3 boss");
        assertTrue(out.contains("SQUAD - Pilot 1's turn"));
        assertTrue(out.contains("SQUAD - Pilot 2's turn"));
        assertTrue(out.contains("Pilot 2 hits Void Brute for 28 damage!"));
        assertTrue(out.contains("MISSION COMPLETE"));
        assertTrue(out.contains("[*] [*] [*]"));
        assertTrue(out.contains("Correct 6/6"));
        assertTrue(out.contains("Score 182"), "Score is the whole squad's damage");
    }

    @Test
    void squadNeedsALesson() {
        assertTrue(play("2\n5\n").contains("Load a lesson first"));
    }

    @Test
    void duplicatePilotNameIsAskedAgain() throws IOException {
        String out = play("3\n" + lesson(ONE_TERM_LESSON) + "\n2\n1\nLyle\nlyle\nMoises\n1\n/quit\n\n5\n");
        assertTrue(out.contains("lyle is already on the team. Pick another name."));
        assertTrue(out.contains("SQUAD - Lyle's turn"));
        assertTrue(out.contains("Moises"));
        assertTrue(out.contains("MISSION FAILED"));
    }

    @Test
    void downedPilotIsSkippedUntilTheSquadFalls() throws IOException {
        // Every answer is wrong: Pilot 1 falls on turn 9, then Pilot 2 plays alone and falls on turn 11.
        String out = play(squadVsVoidBrute(ONE_TERM_LESSON) + "x\n".repeat(11) + "\n5\n");
        assertTrue(out.contains("Pilot 1 is down! The squad fights on."));
        assertTrue(out.contains("DOWN"));
        assertTrue(out.contains("MISSION FAILED"));
        assertTrue(out.contains("Correct 0/11"));
        assertTrue(out.contains("Goodbye"));
    }
}
