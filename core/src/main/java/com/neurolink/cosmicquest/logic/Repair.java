package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Player;

/** Heals 20 hull, never above the maximum. */
public class Repair extends PowerUp {

    /** Hull restored by one Repair. */
    public static final int REPAIR_AMOUNT = 20;

    /** Creates a Repair. */
    public Repair() {
        super("Repair", "Heals " + REPAIR_AMOUNT + " hull.");
    }

    /** Heals the player; Player.heal already stops at the maximum. */
    @Override
    public void apply(Player player, BattleMode battle) {
        player.heal(REPAIR_AMOUNT);
    }
}
