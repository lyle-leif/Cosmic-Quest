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