package com.cardgames.model.skullking;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the current state of a Skull King game session.
 */
public class SkullKingState {
    private List<SkullKingCard> deck = new ArrayList<>();
    private List<SkullKingPlayer> players = new ArrayList<>();
    private int currentPlayerIndex;
    private int roundNumber = 1;
    private String phase;
    private int trickStarterIndex;
    private String winner;
    private String trickWinner;

    private List<String> readyPlayers = new ArrayList<>();

    /**
     * Default constructor. Initializes the game phase to "BIDDING".
     */
    public SkullKingState() {
        this.phase = "BIDDING";
    }

    /**
     * Gets the current deck of cards.
     *
     * @return The deck.
     */
    public List<SkullKingCard> getDeck() {
        return deck;
    }

    /**
     * Sets the current deck of cards.
     *
     * @param deck The deck to set.
     */
    public void setDeck(List<SkullKingCard> deck) {
        this.deck = deck;
    }

    /**
     * Gets the list of players in the game.
     *
     * @return The list of players.
     */
    public List<SkullKingPlayer> getPlayers() {
        return players;
    }

    /**
     * Sets the list of players in the game.
     *
     * @param players The list of players to set.
     */
    public void setPlayers(List<SkullKingPlayer> players) {
        this.players = players;
    }

    /**
     * Gets the index of the current player in the players list.
     *
     * @return The current player index.
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Sets the index of the current player.
     *
     * @param currentPlayerIndex The player index to set.
     */
    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    /**
     * Gets the current round number (1-10).
     *
     * @return The round number.
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Sets the current round number.
     *
     * @param roundNumber The round number to set.
     */
    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    /**
     * Gets the current phase of the game (e.g., "BIDDING", "PLAYING").
     *
     * @return The game phase.
     */
    public String getPhase() {
        return phase;
    }

    /**
     * Sets the current phase of the game.
     *
     * @param phase The game phase to set.
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * Gets the index of the player who started the current trick.
     *
     * @return The trick starter index.
     */
    public int getTrickStarterIndex() {
        return trickStarterIndex;
    }

    /**
     * Sets the index of the player who started the current trick.
     *
     * @param trickStarterIndex The trick starter index to set.
     */
    public void setTrickStarterIndex(int trickStarterIndex) {
        this.trickStarterIndex = trickStarterIndex;
    }

    /**
     * Gets the username of the game winner.
     *
     * @return The winner's username.
     */
    public String getWinner() {
        return winner;
    }

    /**
     * Sets the username of the game winner.
     *
     * @param winner The winner's username to set.
     */
    public void setWinner(String winner) {
        this.winner = winner;
    }

    /**
     * Gets the list of players who have marked themselves as ready.
     *
     * @return The list of ready players.
     */
    public List<String> getReadyPlayers() {
        return readyPlayers;
    }

    /**
     * Sets the list of ready players.
     *
     * @param readyPlayers The list of ready players to set.
     */
    public void setReadyPlayers(List<String> readyPlayers) {
        this.readyPlayers = readyPlayers;
    }

    /**
     * Gets the username of the player who won the last trick.
     *
     * @return The trick winner's username.
     */
    public String getTrickWinner() {
        return trickWinner;
    }

    /**
     * Sets the username of the player who won the last trick.
     *
     * @param trickWinner The trick winner's username to set.
     */
    public void setTrickWinner(String trickWinner) {
        this.trickWinner = trickWinner;
    }
}
