package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Inventory rules that do not need a battle. Using a power-up needs a battle,
 * so that is tested in PowerUpBattleTest.
 */
class PowerUpInventoryTest {

    private final PowerUpInventory inventory = new PowerUpInventory();

    @Test
    void holdsAtMostThree() {
        assertTrue(inventory.add(new Shield()));
        assertTrue(inventory.add(new Repair()));
        assertTrue(inventory.add(new Hint()));
        assertTrue(inventory.isFull());
        assertFalse(inventory.add(new DoubleDamage()));
        assertEquals(3, inventory.size());
    }

    @Test
    void keepsSlotOrder() {
        inventory.add(new Shield());
        inventory.add(new Repair());
        assertInstanceOf(Shield.class, inventory.getPowerUps().get(0));
        assertInstanceOf(Repair.class, inventory.getPowerUps().get(1));
    }

    @Test
    void lockLastsUntilCountedDown() {
        inventory.add(new Shield());
        assertTrue(inventory.lockRandomSlot(new Random(1), 2));
        assertTrue(inventory.isLocked(0));
        inventory.tickLocks();
        assertTrue(inventory.isLocked(0));
        inventory.tickLocks();
        assertFalse(inventory.isLocked(0));
    }

    @Test
    void lockSkipsAlreadyLockedSlots() {
        inventory.add(new Shield());
        inventory.add(new Repair());
        inventory.lockRandomSlot(new Random(1), 2);
        inventory.lockRandomSlot(new Random(1), 2);
        assertTrue(inventory.isLocked(0) && inventory.isLocked(1));
        assertFalse(inventory.lockRandomSlot(new Random(1), 2));
    }

    @Test
    void nothingToLockWhenEmptyOrZeroTurns() {
        assertFalse(inventory.lockRandomSlot(new Random(1), 2));
        inventory.add(new Shield());
        assertFalse(inventory.lockRandomSlot(new Random(1), 0));
    }

    @Test
    void listCannotBeChanged() {
        inventory.add(new Shield());
        assertThrows(UnsupportedOperationException.class, () -> inventory.getPowerUps().clear());
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> inventory.add(null));
        assertThrows(IllegalArgumentException.class, () -> inventory.isLocked(0));
    }

    @Test
    void randomPowerUpCanBeEveryKind() {
        Random random = new Random(5);
        boolean shield = false;
        boolean repair = false;
        boolean doubleDamage = false;
        boolean hint = false;
        for (int i = 0; i < 100; i++) {
            PowerUp p = PowerUp.random(random);
            shield |= p instanceof Shield;
            repair |= p instanceof Repair;
            doubleDamage |= p instanceof DoubleDamage;
            hint |= p instanceof Hint;
        }
        assertTrue(shield && repair && doubleDamage && hint);
    }

    @Test
    void maskKeepsFirstLetterAndSpaces() {
        assertEquals("A_______ _____", Hint.maskAfterFirstLetter("Abstract class"));
        assertEquals("F____", Hint.maskAfterFirstLetter("Field"));
    }
}
