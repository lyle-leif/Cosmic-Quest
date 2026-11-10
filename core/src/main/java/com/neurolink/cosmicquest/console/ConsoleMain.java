package com.neurolink.cosmicquest.console;

import com.neurolink.cosmicquest.logic.BattleState;
import com.neurolink.cosmicquest.logic.InvalidLessonFormatException;
import com.neurolink.cosmicquest.logic.LessonData;
import com.neurolink.cosmicquest.logic.PowerUp;
import com.neurolink.cosmicquest.logic.QuestionGenerator;
import com.neurolink.cosmicquest.logic.SoloBossBattle;
import com.neurolink.cosmicquest.logic.TurnResult;
import com.neurolink.cosmicquest.logic.TxtParser;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.MultipleChoiceQuestion;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;

/**
 * The console version of the game: the main menu, lesson loading and the
 * Solo battle loop. It reads the player's input and tells
 * {@link ConsoleRenderer} what to print; it never prints by itself.
 *
 * <ul>
 * <li><b>Input (D3):</b> one {@link Scanner} for the whole program, and only
 *     {@code nextLine()}. Numbers are parsed inside {@code try/catch}, so
 *     letters or blank lines never crash the game.</li>
 * <li><b>Timer (D2):</b> {@code nextLine()} cannot be interrupted, so the
 *     time is measured instead. An answer that arrives after
 *     {@link #ANSWER_TIME_LIMIT_SECONDS} counts as a timeout, even if it is right.</li>
 * <li><b>End of input:</b> Ctrl+Z (Windows) or Ctrl+D closes the input; the
 *     game says goodbye instead of crashing.</li>
 * </ul>
 */
public class ConsoleMain {

    /** Seconds the player has to answer each question. */
    public static final int ANSWER_TIME_LIMIT_SECONDS = 30;

    /** The lesson loaded at start-up, relative to the project folder. */
    public static final String DEFAULT_LESSON = "assets/lessons/OOP_Fundamentals.txt";

    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final String COMMAND_PREFIX = "/";
    private static final String QUIT_COMMAND = "/quit";
    private static final String HELP_COMMAND = "/help";
    private static final String DEFAULT_NAME = "Pilot";
    private static final int LOWEST_LEVEL = 1;

    private static final int MENU_SOLO = 1;
    private static final int MENU_SQUAD = 2;
    private static final int MENU_LOAD = 3;
    private static final int MENU_HELP = 4;
    private static final List<String> MAIN_MENU =
            List.of("Solo battle", "Squad battle", "Load lesson", "How to play", "Exit");
    private static final List<String> LEVELS = List.of("Level 1", "Level 2", "Level 3 (boss)");

    /** Every alien, in the order the menu lists them. */
    private static final List<IntFunction<Entity>> ALIENS =
            List.of(VoidBrute::new, StarWeaver::new, NebulaLich::new, LunarBeast::new);

    private final Scanner in;
    private final ConsoleRenderer renderer;
    private final Random random;
    private final LongSupplier clock;
    private QuestionGenerator generator;
    private String lessonName;

    /**
     * Starts the console game with the default lesson.
     *
     * @param args not used.
     */
    public static void main(String[] args) {
        ConsoleMain game = new ConsoleMain(new Scanner(System.in), new ConsoleRenderer(),
                new Random(), System::nanoTime);
        game.loadDefaultLesson();
        game.run();
    }

    /**
     * Prepares the game. Tests pass a scripted Scanner, a renderer that
     * prints to memory, a fixed Random and a fake clock.
     *
     * @param in the only Scanner in the program (D3).
     * @param renderer prints everything.
     * @param random shuffles questions; tests pass a fixed seed.
     * @param clock the current time in nanoseconds, for the answer timer (D2).
     * @throws IllegalArgumentException if an argument is null.
     */
    public ConsoleMain(Scanner in, ConsoleRenderer renderer, Random random, LongSupplier clock) {
        if (in == null || renderer == null || random == null || clock == null) {
            throw new IllegalArgumentException("Scanner, renderer, random and clock must not be null.");
        }
        this.in = in;
        this.renderer = renderer;
        this.random = random;
        this.clock = clock;
    }

    /**
     * Shows the main menu until the player picks Exit or the input closes.
     */
    public void run() {
        renderer.showTitle();
        try {
            boolean running = true;
            while (running) {
                running = handleMenuChoice(readChoice(menuTitle(), MAIN_MENU));
            }
        } catch (NoSuchElementException closed) {
            // Ctrl+Z / Ctrl+D or the end of a script: leave quietly.
            renderer.showMessage("");
        }
        renderer.showMessage("Goodbye, pilot.");
    }

    /**
     * Loads {@link #DEFAULT_LESSON}. The game can be started from the
     * project folder or from {@code core/}, so both places are tried.
     *
     * @return true if a lesson was loaded.
     */
    public boolean loadDefaultLesson() {
        for (Path path : List.of(Path.of(DEFAULT_LESSON), Path.of("..", DEFAULT_LESSON))) {
            if (Files.isRegularFile(path)) {
                return loadLesson(path);
            }
        }
        renderer.showError("Default lesson not found. Choose Load lesson and type its path.");
        return false;
    }

    /**
     * Reads a lesson file and makes it the one battles ask about. Any problem
     * is shown to the player and the previous lesson stays loaded.
     *
     * @param path the lesson file.
     * @return true if it loaded.
     */
    public boolean loadLesson(Path path) {
        try {
            LessonData data = new TxtParser().parseFile(path);
            QuestionGenerator loaded = new QuestionGenerator(data, random);
            if (loaded.getSupportedTypes().isEmpty()) {
                renderer.showError("That lesson has no terms or lists to ask about.");
                return false;
            }
            generator = loaded;
            lessonName = path.getFileName() == null ? path.toString() : path.getFileName().toString();
            renderer.showMessage("Loaded lesson: " + lessonName);
            return true;
        } catch (NoSuchFileException e) {
            renderer.showError("No file found at " + path + ".");
        } catch (InvalidLessonFormatException e) {
            renderer.showError("Bad lesson file. " + e.getMessage());
        } catch (IOException e) {
            renderer.showError("Could not read " + path + ".");
        }
        return false;
    }

    /** Runs one main menu choice. Returns false when the player picks Exit. */
    private boolean handleMenuChoice(int choice) {
        switch (choice) {
            case MENU_SOLO:
                playSolo();
                return true;
            case MENU_SQUAD:
                renderer.showMessage("Squad battles arrive in the next update.");
                return true;
            case MENU_LOAD:
                promptLesson();
                return true;
            case MENU_HELP:
                renderer.showHowToPlay(ANSWER_TIME_LIMIT_SECONDS);
                return true;
            default:
                return false;
        }
    }

    private String menuTitle() {
        return "MAIN MENU  (lesson: " + (lessonName == null ? "none loaded" : lessonName) + ")";
    }

    /** Asks for a lesson path. Quotes from "Copy as path" are removed; blank cancels. */
    private void promptLesson() {
        renderer.showMessage("Type the path to a lesson .txt file (blank to cancel):");
        renderer.showPrompt();
        String typed = readLine().strip().replace("\"", "");
        if (typed.isEmpty()) {
            return;
        }
        try {
            loadLesson(Path.of(typed));
        } catch (InvalidPathException e) {
            renderer.showError("That is not a valid path: " + typed);
        }
    }

    /** Sets up a Solo battle, plays it to the end, then shows the debrief. */
    private void playSolo() {
        if (generator == null) {
            renderer.showError("Load a lesson first (menu 3).");
            return;
        }
        Player player = new Player(readName());
        Entity entity = chooseAlien();
        SoloBossBattle battle = new SoloBossBattle(player, entity, generator, random);
        battle.startBattle();
        renderer.showMessage("");
        renderer.showMessage(entity.getName() + " Lv " + entity.getLevel() + " appears! "
                + entity.getSpecialMove());

        while (!battle.isOver()) {
            playTurn(battle);
        }

        renderer.showDebrief(battle.getState() == BattleState.WON, battle.getStars(),
                player.getScore(), battle.getStats());
        renderer.showMessage("Press Enter to return to the menu.");
        readLine();
    }

    /** Asks for the pilot's name; blank means "Pilot". */
    private String readName() {
        renderer.showMessage("Pilot name (blank for " + DEFAULT_NAME + "):");
        renderer.showPrompt();
        String name = readLine().strip();
        return name.isEmpty() ? DEFAULT_NAME : name;
    }

    /** Lists the aliens this lesson can make questions for, then asks for a level. */
    private Entity chooseAlien() {
        List<IntFunction<Entity>> available = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (IntFunction<Entity> alien : ALIENS) {
            Entity sample = alien.apply(LOWEST_LEVEL);
            if (generator.supports(sample.getQuestionType())) {
                available.add(alien);
                names.add(sample.getName() + " - " + sample.getSpecialMove());
            }
        }
        int alien = readChoice("CHOOSE AN ALIEN", names);
        int level = readChoice("CHOOSE A LEVEL", LEVELS);
        return available.get(alien - 1).apply(level);
    }

    /**
     * Plays one turn: shows the question, reads commands until an answer
     * arrives, then submits it (or a timeout, if it was too slow).
     */
    private void playTurn(SoloBossBattle battle) {
        long start = clock.getAsLong();
        showBattleScreen(battle);
        String answer;
        while (true) {
            renderer.showPrompt();
            answer = readLine().strip();
            if (answer.isEmpty()) {
                renderer.showError("Type an answer, or " + HELP_COMMAND + " for commands.");
            } else if (answer.equalsIgnoreCase(QUIT_COMMAND)) {
                battle.endBattle();
                renderer.showMessage("You retreat from the battle.");
                return;
            } else if (answer.startsWith(COMMAND_PREFIX)) {
                runCommand(battle, answer);
            } else {
                break;
            }
        }

        boolean late = clock.getAsLong() - start > ANSWER_TIME_LIMIT_SECONDS * NANOS_PER_SECOND;
        TurnResult result;
        if (late) {
            renderer.showMessage("Time's up! Answers must come within "
                    + ANSWER_TIME_LIMIT_SECONDS + " seconds.");
            result = battle.submitTimeout();
        } else {
            result = battle.submitAnswer(answer);
        }
        showTurnResult(battle, result);
        if (!battle.isOver()) {
            battle.nextTurn();
        }
    }

    /** The HUD, the question (minus options a Hint ruled out), any hint, and the power-ups. */
    private void showBattleScreen(SoloBossBattle battle) {
        renderer.showHud(battle.getPlayer(), battle.getEntity());
        renderer.showQuestion(battle.getCurrentQuestion(), battle.getHiddenOptions());
        if (battle.getHint() != null) {
            renderer.showMessage("Hint: " + battle.getHint());
        }
        renderer.showPowerUps(battle.getInventory());
    }

    /** Runs a battle command such as /2 or /help. */
    private void runCommand(SoloBossBattle battle, String command) {
        if (command.equalsIgnoreCase(HELP_COMMAND)) {
            renderer.showHowToPlay(ANSWER_TIME_LIMIT_SECONDS);
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(command.substring(COMMAND_PREFIX.length()));
        } catch (NumberFormatException e) {
            renderer.showError("Unknown command " + command + ". Try " + HELP_COMMAND + ".");
            return;
        }
        try {
            PowerUp used = battle.usePowerUp(slot - 1);
            renderer.showMessage(used.getName() + " used: " + used.getDescription());
            showBattleScreen(battle);
        } catch (IllegalArgumentException e) {
            renderer.showError("There is no power-up in slot " + slot + ".");
        } catch (IllegalStateException e) {
            renderer.showError(e.getMessage());
        }
    }

    /** Prints what happened in a turn: the result, both hits, Hex Bolt and any reward. */
    private void showTurnResult(SoloBossBattle battle, TurnResult result) {
        Player player = battle.getPlayer();
        Entity entity = battle.getEntity();
        renderer.showAnswerResult(result.isCorrect(), result.getQuestion());
        if (!result.isCorrect() && result.getMatchRatio() > 0) {
            renderer.showPartialMatch(result.getMatchRatio());
        }
        if (result.getDamageDealt() > 0) {
            renderer.showHit(player.getName(), entity.getName(), result.getDamageDealt());
        }
        boolean alienStruck = !result.isCorrect() && result.getNewState() != BattleState.WON;
        if (alienStruck) {
            if (result.getDamageTaken() > 0) {
                renderer.showHit(entity.getName(), player.getName(), result.getDamageTaken());
            } else {
                renderer.showMessage("Your shield blocks the strike!");
            }
            if (entity.reshufflesChoices() && result.getQuestion() instanceof MultipleChoiceQuestion) {
                renderer.showMessage("Hex Bolt! That question's choices are reshuffled.");
            }
        }
        if (result.getPowerUpEarned() != null) {
            renderer.showMessage("Power-up earned: " + result.getPowerUpEarned().getName() + "!");
        }
    }

    /**
     * Shows a numbered menu and reads a choice, asking again until it is a
     * number in range.
     *
     * @return the choice, from 1.
     */
    private int readChoice(String title, List<String> options) {
        renderer.showMenu(title, options);
        while (true) {
            renderer.showPrompt();
            String typed = readLine().strip();
            try {
                int choice = Integer.parseInt(typed);
                if (choice >= 1 && choice <= options.size()) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // Not a number: fall through to the same message.
            }
            renderer.showError("Type a number from 1 to " + options.size() + ".");
        }
    }

    /**
     * Reads one line. The only place that reads input (D3).
     *
     * @throws NoSuchElementException if the input is closed; {@link #run()} ends the game.
     */
    private String readLine() {
        return in.nextLine();
    }
}
