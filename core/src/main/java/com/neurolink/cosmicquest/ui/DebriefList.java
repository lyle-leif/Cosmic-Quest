package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.neurolink.cosmicquest.logic.MatchStats;
import java.util.List;

/**
 * The missed-questions review shared by the Victory and Game Over screens.
 *
 * <p>Each miss is its own block (question, your answer, correct answer) separated by a thin
 * divider. The list fills whatever space its cell gives it and only shows a (vertical) scroll
 * bar when the blocks are taller than that space. Text wraps to the box width.
 */
public final class DebriefList {

    private DebriefList() {
    }

    /**
     * Builds the review area. Add the result to a cell with {@code grow()} so it takes all the
     * free height.
     *
     * @param emptyMessage shown, centred, when there are no misses
     * @param emptyStyle   label style for the empty message
     */
    public static Actor build(Skin skin, List<MatchStats.Miss> misses, String emptyMessage, String emptyStyle) {
        Table list = new Table();
        list.top().pad(12f, 16f, 12f, 16f);

        if (misses.isEmpty()) {
            Label empty = new Label(emptyMessage, skin, emptyStyle);
            empty.setWrap(true);
            empty.setAlignment(Align.center);
            list.center();
            list.add(empty).growX();
        } else {
            for (int i = 0; i < misses.size(); i++) {
                MatchStats.Miss m = misses.get(i);
                if (i > 0) {
                    list.add(new Image(skin.getDrawable("divider"))).growX().height(1f).padTop(10f).padBottom(10f).row();
                }
                String picked = m.getPlayerAnswer().isBlank() ? "(Timed out)" : m.getPlayerAnswer();

                Label q = new Label("Q: " + m.getQuestion().getQuestionText(), skin, "small");
                q.setWrap(true);
                Label yours = new Label("Your answer:  " + picked, skin, "small");
                yours.setWrap(true);
                yours.setColor(CosmicTheme.BLOOD_RED);
                Label correct = new Label("Correct:  " + m.getQuestion().getCorrectAnswer(), skin, "small");
                correct.setWrap(true);
                correct.setColor(CosmicTheme.ASTRAL_CYAN);

                list.add(q).growX().left().padBottom(4f).row();
                list.add(yours).growX().left().padLeft(14f).padBottom(2f).row();
                list.add(correct).growX().left().padLeft(14f).row();
            }
        }

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setFlickScroll(true);
        return scroll;
    }
}
