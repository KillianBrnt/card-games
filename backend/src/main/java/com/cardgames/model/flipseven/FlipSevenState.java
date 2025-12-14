package com.cardgames.model.flipseven;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FlipSevenState {
    private List<Card> deck = new ArrayList<>();
    private List<FlipSevenPlayer> players = new ArrayList<>();
    private int currentPlayerIndex;
    private boolean gameCheck; // True if waiting for something? Or maybe "roundActive"
    private String pendingActionType; // "FREEZE_SELECTION", "FLIP3_SELECTION"
    private String pendingActionInitiator; // Username of player who drew the card

    @JsonProperty("isRoundOver")
    private boolean isRoundOver = false;

    @JsonProperty("readyPlayers")
    private List<String> readyPlayers = new ArrayList<>();

    @JsonProperty("winner")
    private String winner;

    @JsonProperty("isGameOver")
    private boolean isGameOver = false;

    public List<Card> getDeck() {
        return deck;
    }

    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    public List<FlipSevenPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<FlipSevenPlayer> players) {
        this.players = players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public boolean isGameCheck() {
        return gameCheck;
    }

    public void setGameCheck(boolean gameCheck) {
        this.gameCheck = gameCheck;
    }

    public String getPendingActionType() {
        return pendingActionType;
    }

    public void setPendingActionType(String pendingActionType) {
        this.pendingActionType = pendingActionType;
    }

    public String getPendingActionInitiator() {
        return pendingActionInitiator;
    }

    public void setPendingActionInitiator(String pendingActionInitiator) {
        this.pendingActionInitiator = pendingActionInitiator;
    }

    private int flip3DrawsRemaining;
    private String flip3ActiveTarget;
    private List<String> pendingActionQueue = new ArrayList<>();

    public int getFlip3DrawsRemaining() {
        return flip3DrawsRemaining;
    }

    public void setFlip3DrawsRemaining(int flip3DrawsRemaining) {
        this.flip3DrawsRemaining = flip3DrawsRemaining;
    }

    public String getFlip3ActiveTarget() {
        return flip3ActiveTarget;
    }

    public void setFlip3ActiveTarget(String flip3ActiveTarget) {
        this.flip3ActiveTarget = flip3ActiveTarget;
    }

    public List<String> getPendingActionQueue() {
        return pendingActionQueue;
    }

    public void setPendingActionQueue(List<String> pendingActionQueue) {
        this.pendingActionQueue = pendingActionQueue;
    }

    @JsonProperty("isRoundOver")
    public boolean isRoundOver() {
        return isRoundOver;
    }

    @JsonProperty("isRoundOver")
    public void setRoundOver(boolean roundOver) {
        isRoundOver = roundOver;
    }

    public List<String> getReadyPlayers() {
        return readyPlayers;
    }

    public void setReadyPlayers(List<String> readyPlayers) {
        this.readyPlayers = readyPlayers;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    private int roundStarterIndex;

    public int getRoundStarterIndex() {
        return roundStarterIndex;
    }

    public void setRoundStarterIndex(int roundStarterIndex) {
        this.roundStarterIndex = roundStarterIndex;
    }
}
