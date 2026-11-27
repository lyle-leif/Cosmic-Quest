package com.neurolink.cosmicquest.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The 2 to 3 players of a Squad battle, in turn order. The team keeps track
 * of whose turn it is and skips players who are at 0 HP.
 */
public class Team {

    /** Fewest players a Squad battle needs. */
    public static final int MIN_PLAYERS = 2;

    /** Most players a team can have. */
    public static final int MAX_PLAYERS = 3;

    private final String name;
    private final List<Player> players = new ArrayList<>();
    private int turnIndex = -1;

    /**
     * Creates an empty team.
     *
     * @param name the team's name.
     * @throws IllegalArgumentException if the name is null or blank.
     */
    public Team(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The team name must not be blank.");
        }
        this.name = name.strip();
    }

    /**
     * Adds a player to the end of the turn order.
     *
     * @param player the player to add.
     * @throws IllegalArgumentException if player is null, or a player with
     *         the same name (ignoring case) is already on the team.
     * @throws IllegalStateException if the team already has 3 players.
     */
    public void addPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("The player must not be null.");
        }
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("A team has at most " + MAX_PLAYERS + " players.");
        }
        for (Player member : players) {
            if (member.getName().equalsIgnoreCase(player.getName())) {
                throw new IllegalArgumentException(player.getName() + " is already on the team.");
            }
        }
        players.add(player);
    }

    /**
     * Returns true when no player can fight on: every player is at 0 HP. An
     * empty team counts as defeated, since nobody can fight.
     */
    public boolean isDefeated() {
        for (Player player : players) {
            if (!player.isDefeated()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves the turn to the next player who is still fighting, skipping
     * defeated players, and returns them. The first call returns the first
     * player still fighting. The search checks each player at most once, so
     * it can never loop forever.
     *
     * @return the player whose turn it is now.
     * @throws IllegalStateException if every player is defeated.
     */
    public Player getNextAlivePlayer() {
        for (int step = 1; step <= players.size(); step++) {
            int index = (turnIndex + step) % players.size();
            if (!players.get(index).isDefeated()) {
                turnIndex = index;
                return players.get(index);
            }
        }
        throw new IllegalStateException("Every player on " + name + " is defeated.");
    }

    /** Returns the team's name. */
    public String getName() {
        return name;
    }

    /** Returns the players in turn order. The list cannot be changed. */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /** Returns how many players are on the team. */
    public int size() {
        return players.size();
    }

    /** Returns the hull of every player added together. */
    public int getTotalHp() {
        int total = 0;
        for (Player player : players) {
            total += player.getHp();
        }
        return total;
    }

    /** Returns the full hull of every player added together. */
    public int getTotalMaxHp() {
        int total = 0;
        for (Player player : players) {
            total += player.getMaxHp();
        }
        return total;
    }

    /** Returns the score of every player added together. */
    public int getTotalScore() {
        int total = 0;
        for (Player player : players) {
            total += player.getScore();
        }
        return total;
    }
}
