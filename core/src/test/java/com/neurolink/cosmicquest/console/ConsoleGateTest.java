package com.neurolink.cosmicquest.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.logic.MatchStats;
import com.neurolink.cosmicquest.logic.PowerUpInventory;
import com.neurolink.cosmicquest.model.Question;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * Console gate (S08): whole games played through {@link ConsoleMain} with the
 * real sample lesson, from the main menu to the debrief and back.
 *
 * <p>Menu choices are typed from a script. Battle answers are typed by a
 * "pilot" that looks at the question the game just showed and answers it
 * right, wrong, at random, or with a power-up command. Every game must end in
 * a debrief, and nothing may crash or loop forever.
 */
class ConsoleGateTest {

    private static final int ALIENS = 4;
    private static final int LEVELS = 3;
    private static final String WRONG = "zzz";
    /** Far more lines than any real session; reaching it means the game is stuck. */
    private static final int MAX_LINES = 20_000;

    /** A finished battle, as the debrief showed it. */
    private record Debrief(boolean won, int stars) {
    }

    /** Prints like the real renderer and remembers what the pilot needs to see. */
    private static final class GateRenderer extends ConsoleRenderer {
        private Question shown;
        private int powerUpsHeld;
        private final List<Debrief> debriefs = new ArrayList<>();

        GateRenderer(PrintStream out) {
            super(out);
        }

        @Override
        public void showQuestion(Question question, java.util.Set<Integer> hiddenOptions) {
            super.showQuestion(question, hiddenOptions);
            shown = question;
        }

        @Override
        public void showPowerUps(PowerUpInventory inventory) {
            super.showPowerUps(inventory);
            powerUpsHeld = inventory.size();
        }

        @Override
        public void showDebrief(boolean won, int stars, int score, MatchStats stats) {
            super.showDebrief(won, stars, score, stats);
            debriefs.add(new Debrief(won, stars));
        }
    }

    /**
     * Keyboard input made one line at a time: an answer when a question is on
     * screen, otherwise the next scripted menu line. Running out of script
     * closes the input, which the game treats as Ctrl+D.
     */
    private static final class Pilot extends InputStream {
        private final GateRenderer screen;
        private final Deque<String> script;
        private final Function<GateRenderer, String> answerer;
        private byte[] line = new byte[0];
        private int next;
        private int linesTyped;

        Pilot(GateRenderer screen, List<String> script, Function<GateRenderer, String> answerer) {
            this.screen = screen;
            this.script = new ArrayDeque<>(script);
            this.answerer = answerer;
        }

        private boolean fill() {
            if (next < line.length) {
                return true;
            }
            String typed;
            if (screen.shown != null) {
                typed = answerer.apply(screen);
                if (!typed.startsWith("/")) {
                    screen.shown = null; // A command (such as a locked power-up) keeps the question waiting.
                }
            } else if (!script.isEmpty()) {
                typed = script.poll();
            } else {
                return false;
            }
            if (++linesTyped > MAX_LINES) {
                throw new AssertionError("The game is stuck: " + MAX_LINES + " lines typed.");
            }
            line = (typed + "\n").getBytes(StandardCharsets.UTF_8);
            next = 0;
            return true;
        }

        @Override
        public int read() {
            return fill() ? line[next++] : -1;
        }

        @Override
        public int read(byte[] target, int offset, int length) {
            if (length == 0) {
                return 0;
            }
            if (!fill()) {
                return -1;
            }
            int count = Math.min(length, line.length - next);
            System.arraycopy(line, next, target, offset, count);
            next += count;
            return count;
        }

        @Override
        public int available() {
            return line.length - next;
        }
    }

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final GateRenderer screen =
            new GateRenderer(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    /** Runs a whole session with the sample lesson loaded, as {@code main} does. */
    private String session(List<String> script, Function<GateRenderer, String> answerer, long seed) {
        Scanner keyboard = new Scanner(new Pilot(screen, script, answerer), StandardCharsets.UTF_8);
        ConsoleMain game = new ConsoleMain(keyboard, screen, new Random(seed), () -> 0);
        assertTrue(game.loadDefaultLesson(), "The sample lesson must load");
        game.run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String right(GateRenderer screen) {
        return screen.shown.getCorrectAnswer();
    }

    private static String wrong(GateRenderer screen) {
        return WRONG;
    }

    /** Menu 1, default name, the alien, the level. */
    private static List<String> solo(int alien, int level) {
        return List.of("1", "", String.valueOf(alien), String.valueOf(level));
    }

    /** Menu 2, 2 or 3 pilots with default names, the boss. */
    private static List<String> squad(int pilots, int alien) {
        List<String> lines = new ArrayList<>(List.of("2", String.valueOf(pilots - 1)));
        for (int i = 0; i < pilots; i++) {
            lines.add("");
        }
        lines.add(String.valueOf(alien));
        return lines;
    }

    /** One battle, then Enter to leave the debrief. */
    private static void battle(List<String> script, List<String> setup) {
        script.addAll(setup);
        script.add("");
    }

    private void assertCleanSession(String out, int battles) {
        assertEquals(battles, screen.debriefs.size(), "Every battle must reach a debrief");
        assertTrue(out.endsWith("Goodbye, pilot." + System.lineSeparator()), "The session must end cleanly");
        assertFalse(out.contains("Exception"), "No exception may reach the player");
        long wins = screen.debriefs.stream().filter(Debrief::won).count();
        assertEquals(wins, count(out, " MISSION COMPLETE"), "The debrief must say the battle was won");
        assertEquals(battles - wins, count(out, " MISSION FAILED"), "The debrief must say the battle was lost");
    }

    /** The script and the game stayed in step: no menu had to ask again. */
    private static void assertNoRetypes(String out) {
        assertFalse(out.contains("Type a number from"), "A menu got input meant for something else");
    }

    private static long count(String text, String part) {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }

    @Test
    void sampleLessonOffersEveryAlien() {
        String out = session(List.of("1", "", "9", "1", "1", "/quit", "", "5"), ConsoleGateTest::right, 1);
        for (String alien : List.of("Void Brute", "Star Weaver", "Nebula Lich", "Lunar Beast")) {
            assertTrue(out.contains(alien + " - "), alien + " must be in the alien list");
        }
    }

    @Test
    void everyAlienCanBeBeatenAtEveryLevelWithAPerfectScore() {
        List<String> script = new ArrayList<>();
        for (int alien = 1; alien <= ALIENS; alien++) {
            for (int level = 1; level <= LEVELS; level++) {
                battle(script, solo(alien, level));
            }
        }
        script.add("5");

        String out = session(script, ConsoleGateTest::right, 7);

        assertCleanSession(out, ALIENS * LEVELS);
        assertNoRetypes(out);
        for (Debrief debrief : screen.debriefs) {
            assertEquals(new Debrief(true, MatchStats.MAX_STARS), debrief);
        }
        assertFalse(out.contains("Wrong."), "A right answer must never be marked wrong");
    }

    @Test
    void everyAlienWinsWhenEveryAnswerIsWrong() {
        List<String> script = new ArrayList<>();
        for (int alien = 1; alien <= ALIENS; alien++) {
            for (int level = 1; level <= LEVELS; level++) {
                battle(script, solo(alien, level));
            }
        }
        script.add("5");

        String out = session(script, ConsoleGateTest::wrong, 7);

        assertCleanSession(out, ALIENS * LEVELS);
        assertNoRetypes(out);
        for (Debrief debrief : screen.debriefs) {
            assertEquals(new Debrief(false, 0), debrief);
        }
        assertTrue(out.contains("Hex Bolt! That question's choices are reshuffled."),
                "The level 3 Nebula Lich reshuffles a missed question");
    }

    @Test
    void squadCanBeatEveryBoss() {
        List<String> winScript = new ArrayList<>();
        for (int alien = 1; alien <= ALIENS; alien++) {
            battle(winScript, squad(3, alien));
        }
        winScript.add("5");
        String won = session(winScript, ConsoleGateTest::right, 3);
        assertCleanSession(won, ALIENS);
        for (String pilot : List.of("Pilot 1", "Pilot 2", "Pilot 3")) {
            assertTrue(won.contains(pilot + " hits "), pilot + " must get credit for their own hits");
        }
        assertNoRetypes(won);
        screen.debriefs.forEach(d -> assertEquals(new Debrief(true, MatchStats.MAX_STARS), d));
    }

    @Test
    void squadFallsPilotByPilotWhenEveryAnswerIsWrong() {
        List<String> script = new ArrayList<>();
        for (int alien = 1; alien <= ALIENS; alien++) {
            battle(script, squad(2, alien));
        }
        script.add("5");

        String out = session(script, ConsoleGateTest::wrong, 3);

        assertCleanSession(out, ALIENS);
        assertNoRetypes(out);
        screen.debriefs.forEach(d -> assertEquals(new Debrief(false, 0), d));
        assertTrue(out.contains("is down! The squad fights on."));
    }

    /**
     * Many random sessions: random modes, aliens and levels, answers that are
     * right about two times in three, and power-ups used whenever one is held
     * and the coin says so. Every battle must still end in a debrief.
     */
    @Test
    void randomPlayAlwaysReachesADebrief() {
        for (long seed = 1; seed <= 40; seed++) {
            buffer.reset();
            screen.debriefs.clear();
            Random dice = new Random(seed);
            List<String> script = new ArrayList<>();
            int battles = 3;
            for (int i = 0; i < battles; i++) {
                int alien = 1 + dice.nextInt(ALIENS);
                battle(script, dice.nextBoolean()
                        ? solo(alien, 1 + dice.nextInt(LEVELS))
                        : squad(2 + dice.nextInt(2), alien));
            }
            script.add("5");

            String out = session(script, s -> {
                if (s.powerUpsHeld > 0 && dice.nextBoolean()) {
                    return "/" + (1 + dice.nextInt(s.powerUpsHeld));
                }
                return dice.nextInt(3) == 0 ? WRONG : right(s);
            }, seed);

            assertCleanSession(out, battles);
            assertNoRetypes(out);
            for (Debrief debrief : screen.debriefs) {
                assertTrue(debrief.won() || debrief.stars() == 0, "A loss earns no stars (seed " + seed + ")");
            }
        }
    }

    /** One long session the way a player would try everything once. */
    @Test
    void fullSessionFromTheMenu() {
        Path lesson = Path.of("..", ConsoleMain.DEFAULT_LESSON).toAbsolutePath().normalize();
        List<String> script = new ArrayList<>(List.of(
                "4",                                  // how to play
                "3", "missing-lesson.txt",            // bad path: reported, old lesson kept
                "3", "\"" + lesson + "\"",            // quoted path, like Copy as path
                "abc", "0", "6"));                    // bad menu input is asked again
        battle(script, solo(1, 2));
        battle(script, squad(3, 4));
        script.add("5");

        String out = session(script, ConsoleGateTest::right, 11);

        assertCleanSession(out, 2);
        assertTrue(out.contains("HOW TO PLAY"));
        assertTrue(out.contains("No file found at missing-lesson.txt."));
        assertEquals(2, out.split("Loaded lesson: OOP_Fundamentals.txt", -1).length - 1,
                "Loaded at start and again from the typed path");
        assertTrue(out.contains("Type a number from 1 to 5."));
        assertTrue(out.contains("MISSION COMPLETE"));
    }
}
