package com.neurolink.cosmicquest;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.neurolink.cosmicquest.ai.AiClient;
import com.neurolink.cosmicquest.ai.AiConfig;
import com.neurolink.cosmicquest.ai.JavaHttpTransport;
import com.neurolink.cosmicquest.logic.AiQuestionProvider;
import com.neurolink.cosmicquest.logic.BattleMode;
import com.neurolink.cosmicquest.logic.InvalidLessonFormatException;
import com.neurolink.cosmicquest.logic.LessonData;
import com.neurolink.cosmicquest.logic.LocalProvider;
import com.neurolink.cosmicquest.logic.QuestionGenerator;
import com.neurolink.cosmicquest.logic.QuestionProvider;
import com.neurolink.cosmicquest.logic.Term;
import com.neurolink.cosmicquest.logic.TxtParser;
import com.neurolink.cosmicquest.logic.SoloBossBattle;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.VoidBrute;
import com.neurolink.cosmicquest.screens.BattleScreen;
import com.neurolink.cosmicquest.screens.PrologueScreen;
import com.neurolink.cosmicquest.ui.CosmicAssets;
import com.neurolink.cosmicquest.ui.CosmicAudio;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Root LibGDX Game controller for Cosmic Quest (Hades Dialogue x 2D Pixel Battle Edition).
 * Faithful to ConsoleMain's lesson loading, AI question provider chain, and Solo/Squad rules.
 */
public class CosmicQuestGame extends Game {

    /** The four aliens in menu order: Void Brute, Star Weaver, Nebula Lich, Lunar Beast. */
    public static final List<IntFunction<Entity>> ALIENS =
            List.of(VoidBrute::new, StarWeaver::new, NebulaLich::new, LunarBeast::new);

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private CosmicAssets assets;
    private GameSettings settings;

    private final Random random = new Random();
    private AiClient ai;
    private LessonData lessonData;
    private String lessonName = "OOP_Fundamentals.txt";
    private boolean useAstralStaff = false;

    /** Shown as the lesson name when no lesson file could be read and the built-in sample is used. */
    public static final String BUILT_IN_LESSON_NAME = "Built-in sample (lesson file not found)";
    private int totalStarsEarned = 0;

    /** Screen transitions: fade to black, switch screens, fade back in. */
    private static final float TRANSITION_OUT_SECONDS = 0.25f;
    private static final float TRANSITION_IN_SECONDS = 0.35f;
    private ShapeRenderer fadeShapes;
    private final Matrix4 fadeProjection = new Matrix4();
    private Screen pendingScreen;
    private float pendingDelay;
    private float fadeAlpha;
    private int fadeDirection;          // 1 = fading out, -1 = fading in, 0 = idle
    private InputProcessor heldInput;   // the new screen's input, given back when its fade-in ends

    @Override
    public void create() {
        settings = new GameSettings(Gdx.app.getPreferences("CosmicQuestSettings"));
        CosmicAudio.configure(settings);
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        fadeShapes = new ShapeRenderer();
        assets = new CosmicAssets();

        try {
            ai = AiConfig.load(resolveConfigFile()).buildChain(new JavaHttpTransport()).orElse(null);
        } catch (Exception ignored) {
            ai = null;
        }
        loadLessonFile("OOP_Fundamentals.txt");
        showStartScreen();
    }

    /**
     * Opens the first screen after loading: the prologue plays every time the game
     * starts (SKIP / Esc leave it), then the main menu. The loading screen calls this when done.
     */
    public void showStartScreen() {
        transitionTo(new PrologueScreen(this));
    }

    /**
     * Switches to another screen with a short fade through black. Input is ignored while the
     * fade runs, so a double click cannot start two transitions.
     */
    public void transitionTo(Screen next) {
        transitionTo(next, 0f);
    }

    /**
     * Like {@link #transitionTo(Screen)}, but holds the current screen for {@code delaySeconds}
     * first (for example, to let a final hit land before the result screen).
     */
    public void transitionTo(Screen next, float delaySeconds) {
        if (next == null || pendingScreen != null) {
            return;
        }
        Gdx.input.setInputProcessor(null);
        if (getScreen() == null) {
            // First screen of the game: fade in from black.
            swapTo(next);
            fadeAlpha = 1f;
            return;
        }
        pendingScreen = next;
        pendingDelay = Math.max(0f, delaySeconds);
        fadeDirection = 1;
    }

    private void swapTo(Screen next) {
        setScreen(next);
        heldInput = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(null);
        fadeDirection = -1;
    }

    @Override
    public void render() {
        super.render();
        // A slow frame (loading a new screen) must not skip the fade.
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        if (fadeDirection == 1) {
            if (pendingDelay > 0f) {
                pendingDelay -= dt;
            } else {
                fadeAlpha = Math.min(1f, fadeAlpha + dt / TRANSITION_OUT_SECONDS);
                if (fadeAlpha >= 1f) {
                    Screen next = pendingScreen;
                    pendingScreen = null;
                    swapTo(next);
                }
            }
        } else if (fadeDirection == -1) {
            fadeAlpha = Math.max(0f, fadeAlpha - dt / TRANSITION_IN_SECONDS);
            if (fadeAlpha <= 0f) {
                fadeDirection = 0;
                if (Gdx.input.getInputProcessor() == null) {
                    Gdx.input.setInputProcessor(heldInput);
                }
                heldInput = null;
            }
        }
        drawFade();
    }

    private void drawFade() {
        if (fadeAlpha <= 0f) {
            return;
        }
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glViewport(0, 0, w, h);
        fadeProjection.setToOrtho2D(0, 0, w, h);
        fadeShapes.setProjectionMatrix(fadeProjection);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        fadeShapes.begin(ShapeRenderer.ShapeType.Filled);
        fadeShapes.setColor(0f, 0f, 0f, fadeAlpha);
        fadeShapes.rect(0, 0, w, h);
        fadeShapes.end();
    }

    /**
     * Starts a Solo battle and opens its screen. Used by the main menu and by
     * the prologue's last scene.
     *
     * @param stage the current screen's stage, for the "Generating questions..." box.
     * @param pilotName the player's name.
     * @param alienIdx index into {@link #ALIENS}.
     * @param level alien level, 1 to 3.
     * @param onError shows a problem, for example a lesson that cannot make questions.
     */
    public void startSoloBattle(Stage stage, String pilotName, int alienIdx, int level, Consumer<String> onError) {
        try {
            Player player = new Player(pilotName);
            Entity boss = ALIENS.get(alienIdx).apply(level);
            SoloBossBattle battle = new SoloBossBattle(player, boss, createQuestionProvider(), random);
            launchBattle(stage, battle, () -> new BattleScreen(this, battle, alienIdx, level, false), onError);
        } catch (RuntimeException ex) {
            onError.accept(ex.getMessage());
        }
    }

    private static Path resolveConfigFile() {
        for (Path p : List.of(
                Path.of(AiConfig.FILE_NAME),
                Path.of("..", AiConfig.FILE_NAME),
                Path.of("Cosmic-Quest", AiConfig.FILE_NAME))) {
            if (Files.isRegularFile(p)) {
                return p;
            }
        }
        return Path.of(AiConfig.FILE_NAME);
    }

    public String getAiStatusText() {
        return ai == null
                ? "AI questions: off (add a key to config.properties to turn them on)"
                : "AI questions: on (" + ai.getName() + ")";
    }

    public boolean isAiEnabled() {
        return ai != null;
    }

    /**
     * Loads a lesson file and returns null on success, or the ConsoleMain error message on failure.
     */
    public String loadLessonWithError(String rawPath) {
        String cleaned = rawPath == null ? "" : rawPath.strip().replace("\"", "");
        if (cleaned.isEmpty()) {
            return "Path is blank.";
        }
        try {
            Path resolved = resolveLessonPath(cleaned);
            LessonData data = new TxtParser().parseFile(resolved);
            QuestionGenerator loaded = new QuestionGenerator(data, random);
            if (loaded.getSupportedTypes().isEmpty()) {
                return "That lesson has no terms or lists to ask about.";
            }
            this.lessonData = data;
            this.lessonName = resolved.getFileName() == null ? resolved.toString() : resolved.getFileName().toString();
            return null;
        } catch (InvalidPathException e) {
            return "That is not a valid path: " + cleaned;
        } catch (NoSuchFileException e) {
            return "No file found at " + cleaned + ".";
        } catch (InvalidLessonFormatException e) {
            return "Bad lesson file. " + e.getMessage();
        } catch (IOException e) {
            return "Could not read " + cleaned + ".";
        }
    }

    private Path resolveLessonPath(String inputPath) {
        Path direct = Path.of(inputPath);
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        for (Path cand : List.of(
                Path.of("lessons", inputPath),
                Path.of("assets", "lessons", inputPath),
                Path.of("..", "assets", "lessons", inputPath),
                Path.of("Cosmic-Quest", "assets", "lessons", inputPath))) {
            if (Files.isRegularFile(cand)) {
                return cand;
            }
        }
        return direct;
    }

    public boolean loadLessonFile(String path) {
        String err = loadLessonWithError(path);
        if (err == null) {
            return true;
        }
        try {
            LessonData loaded = CosmicAssets.loadLesson(path);
            QuestionGenerator gen = new QuestionGenerator(loaded, random);
            if (!gen.getSupportedTypes().isEmpty()) {
                lessonData = loaded;
                Path p = Path.of(path.replace("\"", "").trim());
                lessonName = p.getFileName() != null ? p.getFileName().toString() : path;
                return true;
            }
        } catch (Exception ignored) {
        }
        if (lessonData == null) {
            lessonData = createFallbackLesson();
            lessonName = BUILT_IN_LESSON_NAME;
        }
        return false;
    }

    private LessonData createFallbackLesson() {
        List<Term> terms = List.of(
            new Term("Class", "A blueprint or template that defines the attributes and behaviors of objects."),
            new Term("Object", "A concrete runtime instance of a class with its own state and identity."),
            new Term("Encapsulation", "Bundling data fields and methods together while restricting direct external access."),
            new Term("Inheritance", "A mechanism where a subclass acquires properties and behaviors from a superclass."),
            new Term("Polymorphism", "The ability of different classes to respond to the same method call in their own way."),
            new Term("Abstraction", "Hiding complex implementation details and exposing only the essential features."),
            new Term("Constructor", "A special block of code invoked to initialize a newly created object."),
            new Term("Interface", "A contract of abstract methods that implementing classes promise to provide.")
        );
        Map<String, List<String>> enums = new LinkedHashMap<>();
        enums.put("Four Pillars of OOP", List.of("Encapsulation", "Inheritance", "Polymorphism", "Abstraction"));
        enums.put("Java Access Modifiers", List.of("public", "protected", "default", "private"));
        return new LessonData(terms, enums);
    }

    /**
     * Runs slow work (it may wait for the AI) on a background thread so the
     * window never freezes, then calls back on the render thread.
     *
     * @param work the slow part, for example {@code battle::startBattle}.
     * @param onDone called on the render thread when the work finished.
     * @param onError called on the render thread with the message if it failed.
     */
    public void runInBackground(Runnable work, Runnable onDone, Consumer<String> onError) {
        Thread worker = new Thread(() -> {
            try {
                work.run();
                Gdx.app.postRunnable(onDone);
            } catch (RuntimeException e) {
                Gdx.app.postRunnable(() -> onError.accept(e.getMessage()));
            }
        }, "cosmic-question-loader");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Starts a battle and opens its screen. With AI on, the first questions
     * are fetched in the background while a "Generating questions..." box is
     * shown; without AI it starts at once.
     *
     * @param stage the current screen's stage, for the waiting box.
     * @param battle the battle to start.
     * @param battleScreen makes the battle screen once the battle is ready.
     * @param onError shows a problem, for example a lesson that cannot make questions.
     */
    public void launchBattle(Stage stage, BattleMode battle, Supplier<Screen> battleScreen, Consumer<String> onError) {
        if (ai == null) {
            try {
                battle.startBattle();
                transitionTo(battleScreen.get());
            } catch (RuntimeException e) {
                onError.accept(e.getMessage());
            }
            return;
        }
        Skin skin = assets.getSkin();
        Dialog waiting = new Dialog("", skin);
        waiting.setBackground(skin.getDrawable("panel-dialogue"));
        waiting.pad(24f, 36f, 24f, 36f);
        waiting.getContentTable().add(new Label("Generating questions...", skin, "gold")).row();
        waiting.getContentTable().add(new Label("Asking " + ai.getName() + ". Local questions are used if it does not answer.",
                skin, "small")).padTop(8f);
        waiting.setModal(true);
        waiting.setMovable(false);
        waiting.show(stage);
        runInBackground(battle::startBattle, () -> {
            waiting.hide(null);
            transitionTo(battleScreen.get());
        }, message -> {
            waiting.hide(null);
            onError.accept(message);
        });
    }

    public QuestionGenerator createGenerator() {
        return new QuestionGenerator(lessonData, random);
    }

    public QuestionProvider createQuestionProvider() {
        LocalProvider local = new LocalProvider(createGenerator());
        if (ai == null) {
            return local;
        }
        return new AiQuestionProvider(ai, lessonData, local);
    }

    public Random getRandom() {
        return random;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapes() {
        return shapes;
    }

    public CosmicAssets getAssets() {
        return assets;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public LessonData getLessonData() {
        return lessonData;
    }

    public String getLessonName() {
        return lessonName;
    }

    public boolean isUseAstralStaff() {
        return useAstralStaff;
    }

    public void setUseAstralStaff(boolean useAstralStaff) {
        this.useAstralStaff = useAstralStaff;
    }

    public int getTotalStarsEarned() {
        return totalStarsEarned;
    }

    public void addStars(int stars) {
        if (stars > 0) {
            this.totalStarsEarned += stars;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        CosmicAudio.disposeMusic();
        if (batch != null) {
            batch.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
        if (fadeShapes != null) {
            fadeShapes.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
    }
}
