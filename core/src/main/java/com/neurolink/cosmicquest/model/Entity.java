package com.neurolink.cosmicquest.model;

/**
 * Abstract base class for all game entities (Player and Aliens).
 */
public abstract class Entity implements Combatant {
    private String name;
    private int level;
    private int hp;
    private int maxHp;
    private int attackPower;

    /**
     * Constructs a new Entity.
     *
     * @param name        the name of the entity
     * @param level       the level of the entity
     * @param hp          the starting hit points
     * @param maxHp       the maximum hit points
     * @param attackPower the base attack power
     */
    protected Entity(String name, int level, int hp, int maxHp, int attackPower) {
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.maxHp = maxHp;
        this.attackPower = attackPower;
    }

    @Override
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        this.hp = Math.max(0, this.hp - amount);
    }

    @Override
    public boolean isDefeated() {
        return this.hp == 0;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getAttackPower() {
        return attackPower;
    }

    /**
     * Attacks the target combatant.
     *
     * @param target the combatant to attack
     */
    public void attack(Combatant target) {
        target.takeDamage(this.attackPower);
    }

    /**
     * Works out how hard this alien strikes when the player misses. By default
     * it is the attack power; aliens with a damage special move override this.
     *
     * @param turn the battle's turn number, from 1.
     * @param itemsMissed list items the player left out (enumeration only, otherwise 0).
     * @return the damage of this strike.
     * @throws IllegalArgumentException if turn or itemsMissed is negative.
     */
    public int getStrikeDamage(int turn, int itemsMissed) {
        if (turn < 0 || itemsMissed < 0) {
            throw new IllegalArgumentException("Turn and items missed must not be negative.");
        }
        return attackPower;
    }

    /**
     * How many turns this alien's strike locks one of the player's power-ups.
     * Most aliens do not lock anything; the Star Weaver overrides this.
     *
     * @return the number of turns, 0 for no lock.
     */
    public int getPowerUpLockTurns() {
        return 0;
    }

    /**
     * Whether this alien's strike reshuffles the options of the multiple
     * choice question the player just missed. Only a level 3 Nebula Lich
     * does (Hex Bolt).
     *
     * @return true if missed questions get new option letters.
     */
    public boolean reshufflesChoices() {
        return false;
    }

    /**
     * Gets the type of question this entity asks.
     *
     * @return the question type
     */
    public abstract QuestionType getQuestionType();

    /**
     * Gets the special move description for this entity.
     *
     * @return the special move description
     */
    public abstract String getSpecialMove();
}