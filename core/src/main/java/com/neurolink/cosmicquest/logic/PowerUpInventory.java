package com.neurolink.cosmicquest.logic;

import com.neurolink.cosmicquest.model.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * One player's power-up slots, at most 3. A slot can be locked for a number
 * of turns (the Star Weaver's Thread Bind); a locked power-up cannot be used.
 */
public class PowerUpInventory {

    /** Most power-ups a player can hold at once. */
    public static final int MAX_SLOTS = 3;

    /** One held power-up and how many more turns it stays locked. */
    private static final class Slot {
        private final PowerUp powerUp;
        private int lockedTurns;

        private Slot(PowerUp powerUp) {
            this.powerUp = powerUp;
        }
    }

    private final List<Slot> slots = new ArrayList<>();

    /**
     * Adds a power-up to the first free slot.
     *
     * @param powerUp the power-up to add.
     * @return true if it was added; false if all slots were full.
     * @throws IllegalArgumentException if powerUp is null.
     */
    public boolean add(PowerUp powerUp) {
        if (powerUp == null) {
            throw new IllegalArgumentException("The power-up must not be null.");
        }
        if (isFull()) {
            return false;
        }
        slots.add(new Slot(powerUp));
        return true;
    }

    /**
     * Uses the power-up in a slot: it takes effect and leaves the inventory.
     *
     * @param index the slot, from 0.
     * @param player the player using it.
     * @param battle the battle it is used in.
     * @return the power-up that was used.
     * @throws IllegalArgumentException if the slot is empty or does not exist.
     * @throws IllegalStateException if the slot is locked.
     */
    public PowerUp use(int index, Player player, BattleMode battle) {
        checkIndex(index);
        if (isLocked(index)) {
            throw new IllegalStateException(slots.get(index).powerUp.getName()
                    + " is locked for " + getLockedTurns(index) + " more turn(s).");
        }
        PowerUp powerUp = slots.remove(index).powerUp;
        powerUp.apply(player, battle);
        return powerUp;
    }

    /**
     * Locks one unlocked power-up, chosen at random.
     *
     * @param random the source of randomness.
     * @param turns how many turns it stays locked.
     * @return true if a power-up was locked; false if there was none to lock.
     */
    public boolean lockRandomSlot(Random random, int turns) {
        List<Slot> unlocked = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot.lockedTurns == 0) {
                unlocked.add(slot);
            }
        }
        if (unlocked.isEmpty() || turns <= 0) {
            return false;
        }
        unlocked.get(random.nextInt(unlocked.size())).lockedTurns = turns;
        return true;
    }

    /** Counts down every lock by one turn. Called once per answered turn. */
    public void tickLocks() {
        for (Slot slot : slots) {
            if (slot.lockedTurns > 0) {
                slot.lockedTurns--;
            }
        }
    }

    /**
     * Tells whether a slot is locked.
     *
     * @param index the slot, from 0.
     * @return true if it cannot be used yet.
     */
    public boolean isLocked(int index) {
        return getLockedTurns(index) > 0;
    }

    /**
     * Returns how many more turns a slot stays locked.
     *
     * @param index the slot, from 0.
     * @return 0 when it is free to use.
     */
    public int getLockedTurns(int index) {
        checkIndex(index);
        return slots.get(index).lockedTurns;
    }

    /** Returns the held power-ups in slot order. The list cannot be changed. */
    public List<PowerUp> getPowerUps() {
        List<PowerUp> powerUps = new ArrayList<>();
        for (Slot slot : slots) {
            powerUps.add(slot.powerUp);
        }
        return Collections.unmodifiableList(powerUps);
    }

    /** Returns how many power-ups are held. */
    public int size() {
        return slots.size();
    }

    /** Returns true when all 3 slots are taken. */
    public boolean isFull() {
        return slots.size() >= MAX_SLOTS;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= slots.size()) {
            throw new IllegalArgumentException("No power-up in slot " + index + ".");
        }
    }
}
