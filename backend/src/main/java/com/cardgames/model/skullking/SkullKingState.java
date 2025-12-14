package com.cardgames.model.skullking;

import java.util.ArrayList;
import java.util.List;

public class SkullKingState {
    private List<SkullKingCard> deck = new ArrayList<>();
    private List<SkullKingPlayer> players = new ArrayList<>();
    private int currentPlayerIndex;
    private int roundNumber = 1; // 1 to 10
    private String phase; // "BIDDING", "PLAYING", "ROUND_OVER", "GAME_OVER"
    private int trickStarterIndex; // Who started the current trick
    private String winner; // Username of game winner
    private String trickWinner;

    public SkullKingState() {
        this.phase = "BIDDING";
    }

    public List<SkullKingCard> getDeck() {
        return deck;
    }

    public void setDeck(List<SkullKingCard> deck) {
        this.deck = deck;
    }

    public List<SkullKingPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<SkullKingPlayer> players) {
        this.players = players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public int getTrickStarterIndex() {
        return trickStarterIndex;
    }

    public void setTrickStarterIndex(int trickStarterIndex) {
        this.trickStarterIndex = trickStarterIndex;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    private List<String> readyPlayers = new ArrayList<>();

    public List<String> getReadyPlayers() {
        return readyPlayers;
    }

    public void setReadyPlayers(List<String> readyPlayers) {
        this.readyPlayers = readyPlayers;
    }

    public String getTrickWinner() {
        return trickWinner;
    }

    public void setTrickWinner(String trickWinner) {
        this.trickWinner = trickWinner;
    }
}
