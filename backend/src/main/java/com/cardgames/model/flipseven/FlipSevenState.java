package com.cardgames.model.flipseven;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the current state of a Flip Seven game session.
 */
public class FlipSevenState {
    private List<Card> deck = new ArrayList<>();
    private List<FlipSevenPlayer> players = new ArrayList<>();
    private int currentPlayerIndex;
    private boolean gameCheck;
    private String pendingActionType;
    private String pendingActionInitiator;

    @JsonProperty("isRoundOver")
    private boolean isRoundOver = false;

    @JsonProperty("readyPlayers")
    private List<String> readyPlayers = new ArrayList<>();

    @JsonProperty("winner")
    private String winner;

    @JsonProperty("isGameOver")
    private boolean isGameOver = false;

    private int flip3DrawsRemaining;
    private String flip3ActiveTarget;
    private List<String> pendingActionQueue = new ArrayList<>();

    private int roundStarterIndex;

    /**
     * Gets the current deck of cards.
     *
     * @return The deck.
     */
    public List<Card> getDeck() {
        return deck;
    }

    /**
     * Sets the current deck of cards.
     *
     * @param deck The deck to set.
     */
    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    /**
     * Gets the list of players in the game.
     *
     * @return The list of players.
     */
    public List<FlipSevenPlayer> getPlayers() {
        return players;
    }

    /**
     * Sets the list of players in the game.
     *
     * @param players The list of players to set.
     */
    public void setPlayers(List<FlipSevenPlayer> players) {
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
     * Checks if a game state check (e.g., waiting for input) is active.
     *
     * @return True if a check is active, false otherwise.
     */
    public boolean isGameCheck() {
        return gameCheck;
    }

    /**
     * Sets the game state check flag.
     *
     * @param gameCheck True to activate check, false otherwise.
     */
    public void setGameCheck(boolean gameCheck) {
        this.gameCheck = gameCheck;
    }

    /**
     * Gets the type of the currently pending action (e.g., "FREEZE", "FLIP3").
     *
     * @return The pending action type.
     */
    public String getPendingActionType() {
        return pendingActionType;
    }

    /**
     * Sets the type of the pending action.
     *
     * @param pendingActionType The action type to set.
     */
    public void setPendingActionType(String pendingActionType) {
        this.pendingActionType = pendingActionType;
    }

    /**
     * Gets the username of the player who initiated the pending action.
     *
     * @return The initiator's username.
     */
    public String getPendingActionInitiator() {
        return pendingActionInitiator;
    }

    /**
     * Sets the username of the player who initiated the pending action.
     *
     * @param pendingActionInitiator The initiator's username to set.
     */
    public void setPendingActionInitiator(String pendingActionInitiator) {
        this.pendingActionInitiator = pendingActionInitiator;
    }

    /**
     * Gets the number of draws remaining for a Flip 3 action.
     *
     * @return The remaining draws.
     */
    public int getFlip3DrawsRemaining() {
        return flip3DrawsRemaining;
    }

    /**
     * Sets the number of draws remaining for a Flip 3 action.
     *
     * @param flip3DrawsRemaining The number of draws to set.
     */
    public void setFlip3DrawsRemaining(int flip3DrawsRemaining) {
        this.flip3DrawsRemaining = flip3DrawsRemaining;
    }

    /**
     * Gets the target player for the active Flip 3 action.
     *
     * @return The target player's username.
     */
    public String getFlip3ActiveTarget() {
        return flip3ActiveTarget;
    }

    /**
     * Sets the target player for the active Flip 3 action.
     *
     * @param flip3ActiveTarget The target username to set.
     */
    public void setFlip3ActiveTarget(String flip3ActiveTarget) {
        this.flip3ActiveTarget = flip3ActiveTarget;
    }

    /**
     * Gets the queue of pending actions to be resolved.
     *
     * @return The pending action queue.
     */
    public List<String> getPendingActionQueue() {
        return pendingActionQueue;
    }

    /**
     * Sets the queue of pending actions.
     *
     * @param pendingActionQueue The action queue to set.
     */
    public void setPendingActionQueue(List<String> pendingActionQueue) {
        this.pendingActionQueue = pendingActionQueue;
    }

    /**
     * Checks if the current round is over.
     *
     * @return True if the round is over, false otherwise.
     */
    @JsonProperty("isRoundOver")
    public boolean isRoundOver() {
        return isRoundOver;
    }

    /**
     * Sets whether the current round is over.
     *
     * @param roundOver True to mark round as over, false otherwise.
     */
    @JsonProperty("isRoundOver")
    public void setRoundOver(boolean roundOver) {
        isRoundOver = roundOver;
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
     * Checks if the entire game is over.
     *
     * @return True if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Sets whether the entire game is over.
     *
     * @param gameOver True to mark game as over, false otherwise.
     */
    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    /**
     * Gets the index of the player who started the current round.
     *
     * @return The round starter index.
     */
    public int getRoundStarterIndex() {
        return roundStarterIndex;
    }

    /**
     * Sets the index of the player who started the current round.
     *
     * @param roundStarterIndex The round starter index to set.
     */
    public void setRoundStarterIndex(int roundStarterIndex) {
        this.roundStarterIndex = roundStarterIndex;
    }
}
