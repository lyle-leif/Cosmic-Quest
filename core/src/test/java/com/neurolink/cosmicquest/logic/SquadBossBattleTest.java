package com.neurolink.cosmicquest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neurolink.cosmicquest.model.Entity;
import com.neurolink.cosmicquest.model.Player;
import com.neurolink.cosmicquest.model.Team;
import com.neurolink.cosmicquest.model.VoidBrute;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SquadBossBattleTest {

    private static final long SEED = 42;
    private static final String WRONG = "definitely not the answer";

    private final Player lyle = new Player("Lyle");
    private final Player moises = new Player("Moises");
    private final Player angelo = new Player("Angelo");

    private static QuestionGenerator generator() {
        return new QuestionGenerator(new LessonData(List.of(
                new Term("Class", "A blueprint that describes the fields and methods of an object."),
                new Term("Object", "An instance of a class that contains data and can perform actions."),
                new Term("Method", "A block of code that performs a specific task."),
                new Term("Field", "A variable that stores data belonging to an object.")),
                Map.of()), new Random(SEED));
    }

    private Team team(Player... players) {
        Team team = new Team("Squad");
        for (Player player : players) {
            team.addPlayer(player);
        }
        return team;
    }

    private SquadBossBattle start(Team team) {
        SquadBossBattle battle = new SquadBossBattle(team, new VoidBrute(3), generator(), new Random(SEED));
        battle.startBattle();
        return battle;
    }

    private static TurnResult answerRight(SquadBossBattle battle) {
        return battle.submitAnswer(battle.getCurrentQuestion().getCorrectAnswer());
    }

    @Test
    void playersTakeTurnsInOrder() {
        SquadBossBattle battle = start(team(lyle, moises, angelo));
        assertSame(lyle, battle.getCurrentPlayer());
        answerRight(battle);
        battle.nextTurn();
        assertSame(moises, battle.getCurrentPlayer());
        answerRight(battle);
        battle.nextTurn();
        assertSame(angelo, battle.getCurrentPlayer());
        answerRight(battle);
        battle.nextTurn();
        assertSame(lyle, battle.getCurrentPlayer());
    }

    @Test
    void missOnlyHurtsThePlayerWhoAnswered() {
        SquadBossBattle battle = start(team(lyle, moises));
        TurnResult result = battle.submitAnswer(WRONG);
        assertEquals(18, result.getDamageTaken());
        assertEquals(82, lyle.getHp());
        assertEquals(100, moises.getHp());
    }

    @Test
    void defeatedPlayerIsSkipped() {
        moises.takeDamage(moises.getMaxHp());
        SquadBossBattle battle = start(team(lyle, moises, angelo));
        assertSame(lyle, battle.getCurrentPlayer());
        answerRight(battle);
        battle.nextTurn();
        assertSame(angelo, battle.getCurrentPlayer());
        answerRight(battle);
        battle.nextTurn();
        assertSame(lyle, battle.getCurrentPlayer());
    }

    @Test
    void oneDownIsNotALossTheSquadFightsOn() {
        SquadBossBattle battle = start(team(lyle, moises));
        lyle.takeDamage(lyle.getMaxHp() - 1);
        battle.submitAnswer(WRONG);
        assertTrue(lyle.isDefeated());
        assertEquals(BattleState.ENEMY_ATTACK, battle.getState());
        battle.nextTurn();
        assertSame(moises, battle.getCurrentPlayer());
        answerRight(battle);
        battle.nextTurn();
        assertSame(moises, battle.getCurrentPlayer(), "Lyle is down, so Moises goes again");
    }

    @Test
    void everyoneDefeatedMeansLost() {
        SquadBossBattle battle = start(team(lyle, moises));
        int turns = 0;
        while (!battle.isOver() && turns < 100) {
            battle.submitAnswer(WRONG);
            turns++;
            if (!battle.isOver()) {
                battle.nextTurn();
            }
        }
        assertEquals(BattleState.LOST, battle.getState());
        assertTrue(lyle.isDefeated());
        assertTrue(moises.isDefeated());
        assertEquals(0, battle.getStars());
    }

    @Test
    void eachPlayerKeepsTheirOwnStreakAndPowerUps() {
        SquadBossBattle battle = start(team(lyle, moises));
        for (int turn = 1; turn <= 5; turn++) {
            answerRight(battle);
            battle.nextTurn();
        }
        // Lyle answered turns 1, 3, 5; Moises turns 2, 4.
        assertEquals(3, lyle.getStreak());
        assertEquals(2, moises.getStreak());
        assertEquals(1, battle.getInventory(lyle).size(), "Lyle's 3rd streak earns his power-up");
        assertEquals(0, battle.getInventory(moises).size());
        assertSame(moises, battle.getCurrentPlayer());
        assertSame(battle.getInventory(moises), battle.getInventory());
    }

    @Test
    void perfectWinUsesTheWholeTeamsHull() {
        SquadBossBattle battle = start(team(lyle, moises));
        while (true) {
            answerRight(battle);
            if (battle.isOver()) {
                break;
            }
            battle.nextTurn();
        }
        assertEquals(BattleState.WON, battle.getState());
        assertEquals(3, battle.getStars());
        assertEquals(battle.getStats().getDamageDealt(), battle.getTeam().getTotalScore());
    }

    @Test
    void starsCountTheFallenPilotsHullToo() {
        // Lyle is down, so the team has 100 of 200 hull: exactly 50% is not above half.
        lyle.takeDamage(lyle.getMaxHp());
        SquadBossBattle battle = start(team(lyle, moises));
        while (true) {
            answerRight(battle);
            if (battle.isOver()) {
                break;
            }
            battle.nextTurn();
        }
        assertEquals(100, moises.getHp(), "Moises alone is at full hull");
        assertEquals(1, battle.getStars());
    }

    @Test
    void currentPlayerUsesTheirOwnPowerUp() {
        SquadBossBattle battle = start(team(lyle, moises));
        battle.getInventory(moises).add(new Repair());
        assertThrows(IllegalArgumentException.class, () -> battle.usePowerUp(0), "Lyle's slots are empty");
        answerRight(battle);
        battle.nextTurn();
        moises.takeDamage(30);
        battle.usePowerUp(0);
        assertEquals(90, moises.getHp());
    }

    @Test
    void notStartedYet() {
        SquadBossBattle battle = new SquadBossBattle(team(lyle, moises), new VoidBrute(3), generator(),
                new Random(SEED));
        assertNull(battle.getCurrentPlayer());
        assertThrows(IllegalStateException.class, () -> battle.submitAnswer("Class"));
        assertThrows(IllegalStateException.class, () -> battle.usePowerUp(0));
    }

    @Test
    void rejectsBadSetups() {
        QuestionGenerator generator = generator();
        Entity boss = new VoidBrute(3);
        assertThrows(IllegalArgumentException.class, () -> new SquadBossBattle(null, boss, generator));
        assertThrows(IllegalArgumentException.class,
                () -> new SquadBossBattle(team(lyle), boss, generator), "One player is not a squad");
        assertThrows(IllegalArgumentException.class,
                () -> new SquadBossBattle(team(lyle, moises), new VoidBrute(2), generator), "Boss must be level 3");
    }
}
