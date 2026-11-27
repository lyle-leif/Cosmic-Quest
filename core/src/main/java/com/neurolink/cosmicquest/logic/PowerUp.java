package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Player;
import java.util.Random;

/**
 * A one-use bonus the player earns every 3rd correct answer in a row. Each
 * kind decides what it does by overriding {@link #apply(Player, BattleMode)}.
 *
 * <p>Power-ups live in {@code logic}, not {@code model}, because they change
 * the battle; this keeps {@code model} free of {@code logic} imports (decision D1).
 */
public abstract class PowerUp {

    private static final int KINDS = 4;

    private final String name;
    private final String description;

    /**
     * Creates a power-up. Only subclasses can call this.
     *
     * @param name the short name shown to the player.
     * @param description what it does, in one sentence.
     */
    protected PowerUp(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Makes one of the four power-ups at random.
     *
     * @param random the source of randomness; tests pass a fixed seed.
     * @return a new Shield, Repair, Double Damage or Hint.
     */
    public static PowerUp random(Random random) {
        switch (random.nextInt(KINDS)) {
            case 0:
                return new Shield();
            case 1:
                return new Repair();
            case 2:
                return new DoubleDamage();
            default:
                return new Hint();
        }
    }

    /** Returns the short name shown to the player. */
    public String getName() {
        return name;
    }

    /** Returns what the power-up does, in one sentence. */
    public String getDescription() {
        return description;
    }

    /**
     * Uses the power-up.
     *
     * @param player the player who uses it.
     * @param battle the battle it is used in.
     */
    public abstract void apply(Player player, BattleMode battle);
}
