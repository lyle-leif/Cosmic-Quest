package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.logic.BattleMode;
import com.neurolink.cosmicquest.logic.MatchStats;
import com.neurolink.cosmicquest.logic.SoloBossBattle;
import com.neurolink.cosmicquest.logic.SquadBossBattle;
import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.LunarBeast;
import com.neurolink.cosmicquest.model.NebulaLich;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.StarWeaver;
import com.neurolink.cosmicquest.model.Team;
import com.neurolink.cosmicquest.model.VoidBrute;
import com.neurolink.cosmicquest.ui.CosmicAssets;
import com.neurolink.cosmicquest.ui.CosmicAudio;
import com.neurolink.cosmicquest.ui.CosmicTheme;
import java.util.List;

/**
 * Hades x 2D Pixel Defeat ("THERE IS NO ESCAPE") & Missed Questions Debrief Screen.
 */
public class GameOverScreen extends ScreenAdapter {

    private final CosmicQuestGame game;
    private final CosmicAssets assets;
    private final BattleMode battle;
    private final int bossIndex;
    private final int bossLevel;
    private final boolean squadMode;
    private final Stage stage;
    private float animTime = 0f;

    public GameOverScreen(CosmicQuestGame game, BattleMode battle, int bossIndex, int bossLevel, boolean squadMode) {
        this.game = game;
        this.assets = game.getAssets();
        this.battle = battle;
        this.bossIndex = bossIndex;
        this.bossLevel = bossLevel;
        this.squadMode = squadMode;
        this.stage = new Stage(new FitViewport(CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT), game.getBatch());

        buildUi();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    private void buildUi() {
        Skin skin = assets.getSkin();
        MatchStats stats = battle.getStats();
        Entity boss = battle.getEntity();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f, 36f, 24f, 36f);
        stage.addActor(root);

        Table card = new Table();
        card.setBackground(skin.getDrawable("panel-crimson"));
        card.pad(22f, 28f, 22f, 28f);

        Label title = new Label("HULL SHATTERED  //  RETURN TO THE STYX", skin, "huge");
        title.setColor(CosmicTheme.BLOOD_RED);
        card.add(title).left().colspan(2).padBottom(6f).row();

        Label sub = new Label("DEFEATED BY " + boss.getName().toUpperCase() + " (LEVEL " + boss.getLevel() + ")", skin, "gold");
        card.add(sub).left().colspan(2).padBottom(14f).row();

        Table rightCol = new Table();

        Label taunt = new Label("\"Study the ancient scrolls of OOP before challenging the Void again, mortal!\"", skin, "default");
        taunt.setWrap(true);
        rightCol.add(taunt).width(800f).left().padBottom(12f).row();

        Table statsBox = new Table();
        statsBox.setBackground(skin.getDrawable("panel-dark"));
        statsBox.pad(12f);

        statsBox.add(new Label("ACCURACY:", skin, "muted")).left().padRight(16f);
        statsBox.add(new Label(stats.getAccuracyPercent() + "% (" + stats.getCorrectAnswers() + "/" + stats.getTotalAnswers() + ")", skin, "cyan")).left().padRight(36f);
        statsBox.add(new Label("DAMAGE DEALT:", skin, "muted")).left().padRight(16f);
        statsBox.add(new Label(stats.getDamageDealt() + " DMG", skin, "gold")).left().padRight(36f);
        statsBox.add(new Label("BOSS HP LEFT:", skin, "muted")).left().padRight(16f);
        statsBox.add(new Label(boss.getHp() + " / " + boss.getMaxHp() + " HP", skin, "crimson")).left().row();

        rightCol.add(statsBox).growX().padBottom(12f).row();

        Label debriefTitle = new Label("CODEX DEBRIEF -- MISSED QUESTIONS & CORRECT DECREES:", skin, "gold");
        rightCol.add(debriefTitle).left().padBottom(6f).row();

        Table missList = new Table();
        missList.pad(10f);
        List<MatchStats.Miss> misses = stats.getMisses();
        if (misses.isEmpty()) {
            missList.add(new Label("No missed questions recorded.", skin, "muted")).left();
        } else {
            for (MatchStats.Miss m : misses) {
                Label qLbl = new Label("Q: " + m.getQuestion().getQuestionText(), skin, "small");
                qLbl.setWrap(true);
                String picked = m.getPlayerAnswer().isBlank() ? "(Timed Out)" : m.getPlayerAnswer();
                Label yourLbl = new Label("   Your Answer: " + picked, skin, "crimson");
                yourLbl.setWrap(true);
                Label corrLbl = new Label("   Correct Decree: " + m.getQuestion().getCorrectAnswer(), skin, "cyan");
                corrLbl.setWrap(true);

                missList.add(qLbl).width(760f).left().row();
                missList.add(yourLbl).width(760f).left().row();
                missList.add(corrLbl).width(760f).left().padBottom(10f).row();
            }
        }
        ScrollPane scroll = new ScrollPane(missList, skin);
        scroll.setFadeScrollBars(false);
        rightCol.add(scroll).width(790f).height(175f).padBottom(14f).row();

        Table btnRow = new Table();
        TextButton retryBtn = new TextButton("RISE AGAIN", skin);
        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playBoonActivate();
                retryBattle();
            }
        });

        TextButton menuBtn = new TextButton("RETURN TO PANTHEON", skin, "utility");
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                game.setScreen(new MainMenuScreen(game));
            }
        });

        btnRow.add(retryBtn).width(240f).height(52f).padRight(14f);
        btnRow.add(menuBtn).width(250f).height(52f);
        rightCol.add(btnRow).left();

        card.add().width(340f).growY();
        card.add(rightCol).grow();
        root.add(card).grow();
    }

    private void retryBattle() {
        Entity boss;
        switch (bossIndex) {
            case 0:
                boss = new VoidBrute(bossLevel);
                break;
            case 1:
                boss = new StarWeaver(bossLevel);
                break;
            case 2:
                boss = new NebulaLich(bossLevel);
                break;
            default:
                boss = new LunarBeast(bossLevel);
                break;
        }
        if (squadMode) {
            Team team = new Team("Squad");
            if (battle instanceof SquadBossBattle prevSquad) {
                for (Player p : prevSquad.getTeam().getPlayers()) {
                    team.addPlayer(new Player(p.getName()));
                }
            } else {
                team.addPlayer(new Player("Pilot 1"));
                team.addPlayer(new Player("Pilot 2"));
            }
            SquadBossBattle b = new SquadBossBattle(team, boss, game.createQuestionProvider(), game.getRandom());
            b.startBattle();
            game.setScreen(new BattleScreen(game, b, bossIndex, bossLevel, true));
        } else {
            String pName = (battle instanceof SoloBossBattle prevSolo)
                    ? prevSolo.getPlayer().getName()
                    : "Pilot";
            SoloBossBattle b = new SoloBossBattle(new Player(pName), boss, game.createQuestionProvider(), game.getRandom());
            b.startBattle();
            game.setScreen(new BattleScreen(game, b, bossIndex, bossLevel, false));
        }
    }

    @Override
    public void render(float delta) {
        animTime += delta;
        ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN);

        stage.getViewport().apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);

        Texture bg = assets.loadTexture("backgrounds/env_wreckage_drift.png", false);
        batch.begin();
        batch.draw(bg, 0, 0, CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT);
        batch.end();

        stage.act(delta);
        stage.draw();

        Entity boss = battle.getEntity();
        float bob = MathUtils.sin(animTime * 3.0f) * 4f;
        batch.begin();
        // Anchor the boss bust portrait flush on the bottom border of the card (y = 27f) so it never floats cut off
        batch.draw(assets.getBossPortrait(boss), 46f, 27f, 320f, 320f);
        batch.draw(assets.getBossPixelSprite(boss), 232f, 44f - bob, 136f, 136f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
