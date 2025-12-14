package com.cardgames.model.flipseven;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FlipSevenPlayer {
    @JsonProperty("username")
    private String username;

    @JsonProperty("hand")
    private List<Card> hand = new ArrayList<>(); // Cards in current round

    @JsonProperty("bankedCards")
    private List<Card> bankedCards = new ArrayList<>(); // Cards banked (if we want to track them history)

    @JsonProperty("roundScore")
    private int roundScore;

    @JsonProperty("totalScore")
    private int totalScore;

    @JsonProperty("isRoundActive")
    private boolean isRoundActive; // True if hasn't busted or stayed yet

    @JsonProperty("hasSecondChance")
    private boolean hasSecondChance; // Modifier active

    @JsonProperty("lastRoundScore")
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
