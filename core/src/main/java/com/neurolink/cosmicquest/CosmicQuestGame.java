package com.neurolink.cosmicquest;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.neurolink.cosmicquest.screens.MainMenuScreen;
import com.neurolink.cosmicquest.ui.CosmicAssets;
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
import java.util.function.Supplier;

/**
 * Root LibGDX Game controller for Cosmic Quest (Hades Dialogue x 2D Pixel Battle Edition).
 * Faithful to ConsoleMain's lesson loading, AI question provider chain, and Solo/Squad rules.
 */
public class CosmicQuestGame extends Game {

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private CosmicAssets assets;

    private final Random random = new Random();
    private AiClient ai;
    private LessonData lessonData;
    private String lessonName = "OOP_Fundamentals.txt";
    private boolean useAstralStaff = false;

    /** Shown as the lesson name when no lesson file could be read and the built-in sample is used. */
    public static final String BUILT_IN_LESSON_NAME = "Built-in sample (lesson file not found)";
    private int totalStarsEarned = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        assets = new CosmicAssets();

        try {
            ai = AiConfig.load(resolveConfigFile()).buildChain(new JavaHttpTransport()).orElse(null);
        } catch (Exception ignored) {
            ai = null;
        }
        loadLessonFile("OOP_Fundamentals.txt");
        setScreen(new MainMenuScreen(this));
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
                setScreen(battleScreen.get());
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
            setScreen(battleScreen.get());
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
        if (batch != null) {
            batch.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
    }
}
