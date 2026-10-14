package com.neurolink.cosmicquest.model;

/**
 * Interface for any entity that can participate in combat.
 */
public interface Combatant {
    void takeDamage(int amount);

    boolean isDefeated();

    int getHp();

    int getMaxHp();
}