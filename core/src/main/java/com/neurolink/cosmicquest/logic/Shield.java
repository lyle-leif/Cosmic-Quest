package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Player;

/** Blocks the next hit the player would take. */
public class Shield extends PowerUp {

    private static final int HITS_BLOCKED = 1;

    /** Creates a Shield. */
    public Shield() {
        super("Shield", "Blocks the next hit.");
    }

    /** Turns on the player's shield for one hit. */
    @Override
    public void apply(Player player, BattleMode battle) {
        player.activateShield(HITS_BLOCKED);
    }
}
