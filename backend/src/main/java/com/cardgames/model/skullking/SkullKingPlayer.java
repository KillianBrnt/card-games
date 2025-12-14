package com.cardgames.model.skullking;

import java.util.ArrayList;
import java.util.List;

public class SkullKingPlayer {
    private String username;
    private List<SkullKingCard> hand = new ArrayList<>();
    private Integer bid; // Null if not yet bid
    private int tricksWon;
    private int score;
    private int roundPoints; // Score delta for the last completed round
    private SkullKingCard cardPlayed; // Card currently played in the trick

    public SkullKingPlayer() {
    }

    public SkullKingPlayer(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<SkullKingCard> getHand() {
        return hand;
    }

    public void setHand(List<SkullKingCard> hand) {
        this.hand = hand;
    }

    public Integer getBid() {
        return bid;
    }

    public void setBid(Integer bid) {
        this.bid = bid;
    }

    public int getTricksWon() {
        return tricksWon;
    }

    public void setTricksWon(int tricksWon) {
        this.tricksWon = tricksWon;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRoundPoints() {
        return roundPoints;
    }

    public void setRoundPoints(int roundPoints) {
        this.roundPoints = roundPoints;
    }

    public SkullKingCard getCardPlayed() {
        return cardPlayed;
    }

    public void setCardPlayed(SkullKingCard cardPlayed) {
        this.cardPlayed = cardPlayed;
    }
}
