package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.logic.BattleMode;
import com.neurolink.cosmicquest.ui.CosmicTheme;

public class BattleScreen extends ScreenAdapter {
    public BattleScreen(CosmicQuestGame game, BattleMode battle, int bossIndex, int bossLevel, boolean squadMode) {}
    @Override public void render(float delta) { ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN); }
}
