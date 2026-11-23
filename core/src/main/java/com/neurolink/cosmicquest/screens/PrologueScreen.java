package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.ui.CosmicAudio;
import com.neurolink.cosmicquest.ui.CosmicTheme;

/**
 * The prologue: plays the finished prologue video ({@code video/prologue.webm}), with its own
 * narration, character voices, music and burned-in subtitles, then returns to the main menu.
 *
 * <p>SKIP or Esc leaves at any time. When the video ends, is skipped, or cannot be played
 * (missing file or no video support), the player goes to the main menu. The prologue never
 * starts a battle.
 */
public class PrologueScreen extends ScreenAdapter {

    /** Path of the prologue video inside the assets folder. */
    public static final String VIDEO_PATH = "video/prologue.webm";

    private final CosmicQuestGame game;
    private final Stage stage;

    private VideoPlayer video;
    private volatile boolean videoFinished;
    private boolean leaving;
    private boolean exiting;

    public PrologueScreen(CosmicQuestGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT), game.getBatch());
        buildUi();
        if (!startVideo()) {
            leaving = true;  // nothing to show: go straight to the main menu
        }
    }

    @Override
    public void show() {
        // The video carries its own soundtrack, so the background music steps aside.
        CosmicAudio.stopMusic();
        InputMultiplexer input = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    leaving = true;
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(input);
    }

    private void buildUi() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(18f, 24f, 18f, 24f);
        root.top().right();
        stage.addActor(root);

        TextButton skipBtn = new TextButton("SKIP  [ESC]", game.getAssets().getSkin(), "utility");
        skipBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                leaving = true;
            }
        });
        root.add(skipBtn).width(170f).height(36f);
    }

    private boolean startVideo() {
        FileHandle file = findVideo();
        if (file == null) {
            return false;
        }
        try {
            video = VideoPlayerCreator.createVideoPlayer();
            video.setOnCompletionListener(handle -> videoFinished = true);
            video.play(file);
            video.setVolume(CosmicAudio.isMuted() ? 0f : 1f);
            return true;
        } catch (Throwable e) {
            // Missing natives, an unsupported format or a broken file.
            disposeVideo();
            return false;
        }
    }

    private static FileHandle findVideo() {
        for (FileHandle candidate : new FileHandle[] {
            Gdx.files.internal(VIDEO_PATH),
            Gdx.files.internal("assets/" + VIDEO_PATH)}) {
            if (candidate.exists()) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Marks the prologue as seen and fades to the main menu (end of video, SKIP, Esc or no
     * video). The video keeps playing under the fade; {@link #hide()} frees it.
     */
    private void goToMenu() {
        if (exiting) {
            return;
        }
        exiting = true;
        game.markPrologueSeen();
        game.transitionTo(new MainMenuScreen(game));
    }

    @Override
    public void render(float delta) {
        if (leaving || videoFinished) {
            goToMenu();
        }
        ScreenUtils.clear(Color.BLACK);
        stage.getViewport().apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        drawVideo(batch);
        stage.act(delta);
        stage.draw();
    }

    private void drawVideo(SpriteBatch batch) {
        if (video == null) {
            return;
        }
        try {
            video.update();
        } catch (Throwable e) {
            leaving = true;
            return;
        }
        Texture frame = video.getTexture();
        if (frame == null) {
            return;
        }
        float w = CosmicTheme.VIRTUAL_WIDTH;
        float h = CosmicTheme.VIRTUAL_HEIGHT;
        float scale = Math.min(w / frame.getWidth(), h / frame.getHeight());
        float dw = frame.getWidth() * scale;
        float dh = frame.getHeight() * scale;
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(frame, (w - dw) / 2f, (h - dh) / 2f, dw, dh);
        batch.end();
    }

    // ------------------------------------------------------------------ lifecycle

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        // Screens are never disposed by the game, and this one is not shown again (each replay
        // makes a new PrologueScreen), so free the video decoder and the stage here.
        disposeVideo();
        stage.dispose();
    }

    private void disposeVideo() {
        if (video != null) {
            try {
                video.dispose();
            } catch (Throwable ignored) {
                // Nothing more to free.
            }
            video = null;
        }
    }
}
