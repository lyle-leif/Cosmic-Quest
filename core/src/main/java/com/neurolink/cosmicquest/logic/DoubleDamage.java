package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Player;

/** The player's next hit deals double damage. */
public class DoubleDamage extends PowerUp {

    /** Creates a Double Damage. */
    public DoubleDamage() {
        super("Double Damage", "Your next hit deals double damage.");
    }

    /** Arms the battle so the next hit that deals damage is doubled. */
    @Override
    public void apply(Player player, BattleMode battle) {
        battle.activateDoubleDamage();
    }
}
