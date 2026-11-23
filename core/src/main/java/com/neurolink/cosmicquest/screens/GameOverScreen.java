package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
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
import com.neurolink.cosmicquest.ui.CardArt;
import com.neurolink.cosmicquest.ui.CosmicTheme;
import com.neurolink.cosmicquest.ui.DebriefList;
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
    private Table card;

    public GameOverScreen(CosmicQuestGame game, BattleMode battle, int bossIndex, int bossLevel, boolean squadMode) {
        this.game = game;
        this.assets = game.getAssets();
        this.battle = battle;
        this.bossIndex = bossIndex;
        this.bossLevel = bossLevel;
        this.squadMode = squadMode;
        this.stage = new Stage(CosmicTheme.newViewport(), game.getBatch());

        buildUi();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        CosmicAudio.playMusic(CosmicAudio.MENU_THEME);
    }

    private void buildUi() {
        Skin skin = assets.getSkin();
        MatchStats stats = battle.getStats();
        Entity boss = battle.getEntity();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f, 36f, 24f, 36f);
        stage.addActor(root);

        card = new Table();
        card.setBackground(skin.getDrawable("panel-crimson"));
        card.pad(22f, 28f, 22f, 28f);

        Label title = new Label("HULL SHATTERED  //  RETURN TO THE STYX", skin, "huge");
        title.setColor(CosmicTheme.BLOOD_RED);
        card.add(title).left().colspan(2).padBottom(6f).row();

        Label sub = new Label("DEFEATED BY " + boss.getName().toUpperCase() + " (LEVEL " + boss.getLevel() + ")", skin, "gold");
        card.add(sub).left().colspan(2).padBottom(14f).row();

        Table rightCol = new Table();
        rightCol.top();

        Label taunt = new Label("\"Study the ancient scrolls of OOP before challenging the Void again, mortal!\"", skin, "default");
        taunt.setWrap(true);
        rightCol.add(taunt).growX().left().padBottom(12f).row();

        // Stats row: label/value pairs, value columns share the spare width
        Table statsBox = new Table();
        statsBox.setBackground(skin.getDrawable("panel-dark"));
        statsBox.pad(14f, 20f, 14f, 20f);

        statsBox.add(new Label("ACCURACY:", skin, "muted")).left().padRight(12f);
        statsBox.add(new Label(stats.getAccuracyPercent() + "% (" + stats.getCorrectAnswers() + "/" + stats.getTotalAnswers() + ")", skin, "cyan")).left().expandX().padRight(20f);
        statsBox.add(new Label("DAMAGE DEALT:", skin, "muted")).left().padRight(12f);
        statsBox.add(new Label(stats.getDamageDealt() + " DMG", skin, "gold")).left().expandX().padRight(20f);
        statsBox.add(new Label("BOSS HP LEFT:", skin, "muted")).left().padRight(12f);
        statsBox.add(new Label(boss.getHp() + " / " + boss.getMaxHp() + " HP", skin, "crimson")).left().expandX().row();

        rightCol.add(statsBox).growX().padBottom(14f).row();

        // Missed-questions review: takes all the height left above the buttons
        Label debriefTitle = new Label("CODEX DEBRIEF -- MISSED QUESTIONS & CORRECT DECREES:", skin, "gold");
        rightCol.add(debriefTitle).left().padBottom(6f).row();
        List<MatchStats.Miss> misses = stats.getMisses();
        rightCol.add(DebriefList.build(skin, misses, "No missed questions recorded.", "muted"))
                .grow().padBottom(14f).row();

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
                game.transitionTo(new MainMenuScreen(game));
            }
        });

        btnRow.add(retryBtn).width(240f).height(52f).padRight(14f);
        btnRow.add(menuBtn).width(250f).height(52f);
        rightCol.add(btnRow).left();

        card.add().width(CardArt.slotWidth(card)).growY();
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
            game.launchBattle(stage, b, () -> new BattleScreen(game, b, bossIndex, bossLevel, true),
                    problem -> game.transitionTo(new MainMenuScreen(game)));
        } else {
            String pName = (battle instanceof SoloBossBattle prevSolo)
                    ? prevSolo.getPlayer().getName()
                    : "Pilot";
            SoloBossBattle b = new SoloBossBattle(new Player(pName), boss, game.createQuestionProvider(), game.getRandom());
            game.launchBattle(stage, b, () -> new BattleScreen(game, b, bossIndex, bossLevel, false),
                    problem -> game.transitionTo(new MainMenuScreen(game)));
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
        CosmicTheme.drawCover(batch, bg, stage.getViewport());
        batch.end();

        stage.act(delta);
        stage.draw();

        // Boss bust portrait sits flush on the card's bottom border and scales with the card
        Entity boss = battle.getEntity();
        float s = CardArt.scale(card, 320f);
        float px = card.getX() + 10f;
        float py = card.getY() + 3f;
        float bob = MathUtils.sin(animTime * 3.0f) * 4f;
        batch.begin();
        batch.draw(assets.getBossPortrait(boss), px, py, 320f * s, 320f * s);
        batch.draw(assets.getBossPixelSprite(boss), px + 186f * s, py + 17f * s - bob, 136f * s, 136f * s);
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
