package com.cardgames.model.flipseven;

import java.util.ArrayList;
import java.util.List;

public class FlipSevenPlayer {
    private String username;
    private List<Card> hand = new ArrayList<>(); // Cards in current round
    private List<Card> bankedCards = new ArrayList<>(); // Cards banked (if we want to track them history)
    private int roundScore;
    private int totalScore;
    private boolean isRoundActive; // True if hasn't busted or stayed yet
    private boolean hasSecondChance; // Modifier active
    private int lastRoundScore;

    public FlipSevenPlayer() {
    }

    public FlipSevenPlayer(String username) {
        this.username = username;
        this.isRoundActive = true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public List<Card> getBankedCards() {
        return bankedCards;
    }

    public void setBankedCards(List<Card> bankedCards) {
        this.bankedCards = bankedCards;
    }

    public int getRoundScore() {
        return roundScore;
    }

    public void setRoundScore(int roundScore) {
        this.roundScore = roundScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public boolean isRoundActive() {
        return isRoundActive;
    }

    public void setRoundActive(boolean roundActive) {
        isRoundActive = roundActive;
    }

    public boolean isHasSecondChance() {
        return hasSecondChance;
    }

    public void setHasSecondChance(boolean hasSecondChance) {
        this.hasSecondChance = hasSecondChance;
    }

    public int getLastRoundScore() {
        return lastRoundScore;
    }

    public void setLastRoundScore(int lastRoundScore) {
        this.lastRoundScore = lastRoundScore;
    }
}
