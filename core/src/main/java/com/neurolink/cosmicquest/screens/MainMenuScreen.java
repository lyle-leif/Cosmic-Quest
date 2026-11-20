package com.neurolink.cosmicquest.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.neurolink.cosmicquest.CosmicQuestGame;
import com.neurolink.cosmicquest.ui.CosmicTheme;

public class MainMenuScreen extends ScreenAdapter {
    public MainMenuScreen(CosmicQuestGame game) {}
    @Override public void render(float delta) { ScreenUtils.clear(CosmicTheme.BG_OBSIDIAN); }
}
