package com.neurolink.cosmicquest.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

/**
 * Sizing for the character art drawn in the left column of a result card (Victory / Game Over).
 *
 * <p>The column is a share of the card width, so it grows with the window. The art scales with
 * the column and is capped by the card height, so it always sits on the card's bottom border and
 * never overlaps the title.
 */
public final class CardArt {

    /** Column width at the 1280x720 design size; the art sizes in the screens are tuned to it. */
    public static final float DESIGN_SLOT_WIDTH = 340f;
    private static final float SLOT_SHARE = 0.2815f;
    private static final float SLOT_MIN = 300f;
    private static final float SLOT_MAX = 480f;
    /** Room kept free at the top of the card for the title and rating lines. */
    private static final float TITLE_SPACE = 150f;

    private CardArt() {
    }

    /** Width of the art column for a card, as a layout value. */
    public static Value slotWidth(Actor card) {
        return new Value() {
            @Override
            public float get(Actor context) {
                return slotWidthOf(card);
            }
        };
    }

    public static float slotWidthOf(Actor card) {
        return MathUtils.clamp(card.getWidth() * SLOT_SHARE, SLOT_MIN, SLOT_MAX);
    }

    /**
     * Scale for art designed at {@link #DESIGN_SLOT_WIDTH}, limited so art of the given design
     * height still fits below the title.
     */
    public static float scale(Actor card, float designArtHeight) {
        float s = slotWidthOf(card) / DESIGN_SLOT_WIDTH;
        float maxH = Math.max(120f, card.getHeight() - TITLE_SPACE);
        return Math.min(s, maxH / designArtHeight);
    }
}
