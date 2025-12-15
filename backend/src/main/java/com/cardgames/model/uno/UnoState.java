package com.cardgames.model.uno;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the current state of an Uno game session.
 */
public class UnoState {
    private List<UnoPlayer> players = new ArrayList<>();
    private List<UnoCard> deck = new ArrayList<>();
    private List<UnoCard> discardPile = new ArrayList<>();

    private int currentPlayerIndex = 0;
    private int direction = 1;

    private UnoCard currentTopCard;

    private UnoCardColor currentColor;

    private boolean gameOver = false;
    private String winner;

    private boolean waitingForColorSelection = false;
    private String pendingActionInitiator;

    /**
     * Default constructor.
     */
    public UnoState() {
    }

    /**
     * Gets the list of players in the game.
     *
     * @return The list of players.
     */
    public List<UnoPlayer> getPlayers() {
        return players;
    }

    /**
     * Sets the list of players in the game.
     *
     * @param players The list of players to set.
     */
    public void setPlayers(List<UnoPlayer> players) {
        this.players = players;
    }

    /**
     * Gets the current deck of cards.
     *
     * @return The deck.
     */
    public List<UnoCard> getDeck() {
        return deck;
    }

    /**
     * Sets the current deck of cards.
     *
     * @param deck The deck to set.
     */
    public void setDeck(List<UnoCard> deck) {
        this.deck = deck;
    }

    /**
     * Gets the pile of discarded cards.
     *
     * @return The discard pile.
     */
    public List<UnoCard> getDiscardPile() {
        return discardPile;
    }

    /**
     * Sets the pile of discarded cards.
     *
     * @param discardPile The discard pile to set.
     */
    public void setDiscardPile(List<UnoCard> discardPile) {
        this.discardPile = discardPile;
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
     * Gets the current direction of play.
     * 1 for clockwise, -1 for counter-clockwise.
     *
     * @return The direction.
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Sets the current direction of play.
     *
     * @param direction The direction to set (1 or -1).
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Gets the card currently at the top of the discard pile.
     *
     * @return The current top card.
     */
    public UnoCard getCurrentTopCard() {
        return currentTopCard;
    }

    /**
     * Sets the card currently at the top of the discard pile.
     *
     * @param currentTopCard The top card to set.
     */
    public void setCurrentTopCard(UnoCard currentTopCard) {
        this.currentTopCard = currentTopCard;
    }

    /**
     * Gets the current active color of the game.
     *
     * @return The active color.
     */
    public UnoCardColor getCurrentColor() {
        return currentColor;
    }

    /**
     * Sets the current active color of the game.
     *
     * @param currentColor The active color to set.
     */
    public void setCurrentColor(UnoCardColor currentColor) {
        this.currentColor = currentColor;
    }

    /**
     * Checks if the game is over.
     *
     * @return True if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Sets whether the game is over.
     *
     * @param gameOver True to mark game as over, false otherwise.
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
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
     * Checks if the game is waiting for a player to select a color (after playing a
     * Wild card).
     *
     * @return True if waiting for color selection, false otherwise.
     */
    public boolean isWaitingForColorSelection() {
        return waitingForColorSelection;
    }

    /**
     * Sets whether the game is waiting for a player to select a color.
     *
     * @param waitingForColorSelection True if waiting, false otherwise.
     */
    public void setWaitingForColorSelection(boolean waitingForColorSelection) {
        this.waitingForColorSelection = waitingForColorSelection;
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
}
