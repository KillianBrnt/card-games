package com.cardgames.model.uno;

import java.util.ArrayList;
import java.util.List;

public class UnoState {
    private List<UnoPlayer> players = new ArrayList<>();
    private List<UnoCard> deck = new ArrayList<>();
    private List<UnoCard> discardPile = new ArrayList<>();

    private int currentPlayerIndex = 0;
    private int direction = 1; // 1 for clockwise, -1 for counter-clockwise

    // Top card of the discard pile (redundant if discardPile is ordered, but useful
    // for quick access)
    private UnoCard currentTopCard;

    // Current active color (important for Wild cards)
    private UnoCardColor currentColor;

    private boolean gameOver = false;
    private String winner;

    // Pending actions, e.g. someone waiting to pick a color
    private boolean waitingForColorSelection = false;
    private String pendingActionInitiator; // Username

    // For handling +2 / +4 stacking or forced draw states?
    // Basic rules usually don't allow stacking, so we just handle the draw
    // immediately
    // or set a flag that the next player MUST draw.
    // Simpler: resolve the effect immediately on the next player's turn start or
    // previous player's turn end.

    public UnoState() {
    }

    public List<UnoPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<UnoPlayer> players) {
        this.players = players;
    }

    public List<UnoCard> getDeck() {
        return deck;
    }

    public void setDeck(List<UnoCard> deck) {
        this.deck = deck;
    }

    public List<UnoCard> getDiscardPile() {
        return discardPile;
    }

    public void setDiscardPile(List<UnoCard> discardPile) {
        this.discardPile = discardPile;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public UnoCard getCurrentTopCard() {
        return currentTopCard;
    }

    public void setCurrentTopCard(UnoCard currentTopCard) {
        this.currentTopCard = currentTopCard;
    }

    public UnoCardColor getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(UnoCardColor currentColor) {
        this.currentColor = currentColor;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public boolean isWaitingForColorSelection() {
        return waitingForColorSelection;
    }

    public void setWaitingForColorSelection(boolean waitingForColorSelection) {
        this.waitingForColorSelection = waitingForColorSelection;
    }

    public String getPendingActionInitiator() {
        return pendingActionInitiator;
    }

    public void setPendingActionInitiator(String pendingActionInitiator) {
        this.pendingActionInitiator = pendingActionInitiator;
    }
}
