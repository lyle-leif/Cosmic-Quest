package com.neurolink.cosmicquest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TeamTest {

    private final Team team = new Team("Squad");
    private final Player lyle = new Player("Lyle");
    private final Player moises = new Player("Moises");
    private final Player angelo = new Player("Angelo");

    private void addAll() {
        team.addPlayer(lyle);
        team.addPlayer(moises);
        team.addPlayer(angelo);
    }

    @Test
    void turnsGoInOrderAndWrapAround() {
        addAll();
        assertSame(lyle, team.getNextAlivePlayer());
        assertSame(moises, team.getNextAlivePlayer());
        assertSame(angelo, team.getNextAlivePlayer());
        assertSame(lyle, team.getNextAlivePlayer());
    }

    @Test
    void defeatedPlayerIsSkipped() {
        addAll();
        moises.takeDamage(moises.getMaxHp());
        assertSame(lyle, team.getNextAlivePlayer());
        assertSame(angelo, team.getNextAlivePlayer());
        assertSame(lyle, team.getNextAlivePlayer());
        assertFalse(team.isDefeated());
    }

    @Test
    void allDefeatedStopsInsteadOfLoopingForever() {
        addAll();
        for (Player player : team.getPlayers()) {
            player.takeDamage(player.getMaxHp());
        }
        assertTrue(team.isDefeated());
        assertThrows(IllegalStateException.class, team::getNextAlivePlayer);
    }

    @Test
    void emptyTeamCountsAsDefeated() {
        assertTrue(team.isDefeated());
        assertThrows(IllegalStateException.class, team::getNextAlivePlayer);
    }

    @Test
    void fourthPlayerIsRejected() {
        addAll();
        assertThrows(IllegalStateException.class, () -> team.addPlayer(new Player("Extra")));
        assertEquals(Team.MAX_PLAYERS, team.size());
    }

    @Test
    void sameNameIgnoringCaseIsRejected() {
        team.addPlayer(lyle);
        assertThrows(IllegalArgumentException.class, () -> team.addPlayer(new Player("LYLE")));
        assertEquals(1, team.size());
    }

    @Test
    void rejectsNullPlayerAndBlankName() {
        assertThrows(IllegalArgumentException.class, () -> team.addPlayer(null));
        assertThrows(IllegalArgumentException.class, () -> new Team(" "));
        assertThrows(IllegalArgumentException.class, () -> new Team(null));
    }

    @Test
    void totalsAddUpEveryPlayer() {
        team.addPlayer(lyle);
        team.addPlayer(moises);
        lyle.takeDamage(30);
        lyle.addScore(20);
        moises.addScore(15);
        assertEquals(170, team.getTotalHp());
        assertEquals(200, team.getTotalMaxHp());
        assertEquals(35, team.getTotalScore());
    }

    @Test
    void playerListCannotBeChanged() {
        team.addPlayer(lyle);
        assertThrows(UnsupportedOperationException.class, () -> team.getPlayers().clear());
    }

    @Test
    void nameIsTrimmed() {
        assertEquals("Squad", new Team("  Squad ").getName());
    }
}
