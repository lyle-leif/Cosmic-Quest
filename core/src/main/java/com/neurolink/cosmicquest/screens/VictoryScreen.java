package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
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
 * Hades x 2D Pixel Victory & Debrief Screen.
 */
public class VictoryScreen extends ScreenAdapter {

    private final CosmicQuestGame game;
    private final CosmicAssets assets;
    private final BattleMode battle;
    private final int bossIndex;
    private final int bossLevel;
    private final boolean squadMode;
    private final int starsEarned;
    private final Stage stage;
    private float animTime = 0f;

    public VictoryScreen(CosmicQuestGame game, BattleMode battle, int bossIndex, int bossLevel, boolean squadMode, int starsEarned) {
        this.game = game;
        this.assets = game.getAssets();
        this.battle = battle;
        this.bossIndex = bossIndex;
        this.bossLevel = bossLevel;
        this.squadMode = squadMode;
        this.starsEarned = starsEarned;
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
        card.setBackground(skin.getDrawable("panel-dialogue"));
        card.pad(22f, 28f, 22f, 28f);

        Label title = new Label("GUARDIAN VANQUISHED!", skin, "huge");
        card.add(title).left().colspan(2).padBottom(4f).row();

        String starDisplay = buildStarString(starsEarned);
        Label starLabel = new Label("PANTHEON RATING:  " + starDisplay + "   (" + starsEarned + " / 3 STARS)", skin, "gold");
        card.add(starLabel).left().colspan(2).padBottom(14f).row();

        // Left space reserved for Cadet Portrait + Pixel Sprite, Right side holds Stats & Debrief
        Table rightCol = new Table();

        Label quote = new Label("\"Another titan of the void yields before the Stygian Star-Blade! The knowledge is ours.\"", skin, "default");
        quote.setWrap(true);
        rightCol.add(quote).width(800f).left().padBottom(12f).row();

        // Stats Grid
        Table statsBox = new Table();
        statsBox.setBackground(skin.getDrawable("panel-dark"));
        statsBox.pad(14f);

        statsBox.add(new Label("GUARDIAN DEFEATED:", skin, "muted")).left().padRight(20f);
        statsBox.add(new Label(boss.getName() + " (Level " + boss.getLevel() + ")", skin, "gold")).left().padRight(40f);
        statsBox.add(new Label("ACCURACY:", skin, "muted")).left().padRight(20f);
        statsBox.add(new Label(stats.getAccuracyPercent() + "% (" + stats.getCorrectAnswers() + "/" + stats.getTotalAnswers() + ")", skin, "cyan")).left().row();

        statsBox.add(new Label("DAMAGE DEALT:", skin, "muted")).left().padTop(8f).padRight(20f);
        statsBox.add(new Label(stats.getDamageDealt() + " DMG", skin, "default")).left().padTop(8f).padRight(40f);
        statsBox.add(new Label("HULL DAMAGE TAKEN:", skin, "muted")).left().padTop(8f).padRight(20f);
        statsBox.add(new Label(stats.getDamageTaken() + " HP", skin, "crimson")).left().padTop(8f).row();

        rightCol.add(statsBox).growX().padBottom(12f).row();

        // Missed Questions Review (if any)
        List<MatchStats.Miss> misses = stats.getMisses();
        if (misses.isEmpty()) {
            Label flawless = new Label("FLAWLESS CODEX MASTERY -- Zero questions missed during this trial!", skin, "cyan");
            rightCol.add(flawless).left().padBottom(16f).row();
        } else {
            Label reviewTitle = new Label("TRIAL DEBRIEF (MISSED QUESTIONS TO REVIEW):", skin, "gold");
            rightCol.add(reviewTitle).left().padBottom(6f).row();

            Table missList = new Table();
            missList.pad(8f);
            for (MatchStats.Miss m : misses) {
                Label qLbl = new Label("Q: " + m.getQuestion().getQuestionText(), skin, "small");
                qLbl.setWrap(true);
                String yourAns = m.getPlayerAnswer().isBlank() ? "(Timed Out)" : m.getPlayerAnswer();
                Label aLbl = new Label("   Your Answer: " + yourAns + "   |   Correct Decree: " + m.getQuestion().getCorrectAnswer(), skin, "muted");
                aLbl.setWrap(true);
                missList.add(qLbl).width(760f).left().row();
                missList.add(aLbl).width(760f).left().padBottom(8f).row();
            }
            ScrollPane scroll = new ScrollPane(missList, skin);
            scroll.setFadeScrollBars(false);
            rightCol.add(scroll).width(790f).height(135f).padBottom(14f).row();
        }

        // Action Buttons Row
        Table btnRow = new Table();
        if (!squadMode && bossLevel < 3) {
            TextButton nextLvlBtn = new TextButton("NEXT THREAT (LV." + (bossLevel + 1) + ")", skin);
            nextLvlBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CosmicAudio.playBoonActivate();
                    launchBattle(bossIndex, bossLevel + 1, false);
                }
            });
            btnRow.add(nextLvlBtn).width(240f).height(50f).padRight(12f);
        }

        TextButton rematchBtn = new TextButton("REMATCH GUARDIAN", skin, "utility");
        rematchBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                launchBattle(bossIndex, bossLevel, squadMode);
            }
        });
        btnRow.add(rematchBtn).width(220f).height(50f).padRight(12f);

        TextButton menuBtn = new TextButton("RETURN TO PANTHEON", skin, "utility");
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CosmicAudio.playClick();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        btnRow.add(menuBtn).width(230f).height(50f);

        rightCol.add(btnRow).left();

        card.add().width(340f).growY();
        card.add(rightCol).grow();

        root.add(card).grow();
    }

    private String buildStarString(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < stars ? "[ * ] " : "[ - ] ");
        }
        return sb.toString().trim();
    }

    private void launchBattle(int index, int level, boolean squad) {
        Entity boss;
        switch (index) {
            case 0:
                boss = new VoidBrute(level);
                break;
            case 1:
                boss = new StarWeaver(level);
                break;
            case 2:
                boss = new NebulaLich(level);
                break;
            default:
                boss = new LunarBeast(level);
                break;
        }
        if (squad) {
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
            game.setScreen(new BattleScreen(game, b, index, level, true));
        } else {
            String pName = (battle instanceof SoloBossBattle prevSolo)
                    ? prevSolo.getPlayer().getName()
                    : "Pilot";
            SoloBossBattle b = new SoloBossBattle(new Player(pName), boss, game.createQuestionProvider(), game.getRandom());
            b.startBattle();
            game.setScreen(new BattleScreen(game, b, index, level, false));
        }
    }

    @Override
    public void render(float delta) {
        animTime += delta;
        ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN);

        stage.getViewport().apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);

        Texture bg = assets.loadTexture("backgrounds/env_supernova_bloom.png", false);
        batch.begin();
        batch.draw(bg, 0, 0, CosmicTheme.VIRTUAL_WIDTH, CosmicTheme.VIRTUAL_HEIGHT);
        batch.end();

        stage.act(delta);
        stage.draw();

        float bob = MathUtils.sin(animTime * 3.5f) * 4f;
        batch.begin();
        // Anchor the Cadet bust portrait flush on the bottom border of the card (y = 27f)
        batch.draw(assets.getCadetPortrait(), 48f, 27f, 270f, 360f);
        batch.draw(assets.getCadetPixelSprite(false), 225f, 44f + bob, 152f, 138f);
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
