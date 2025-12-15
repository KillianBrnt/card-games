package com.cardgames.model.skullking;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in a Skull King game session.
 */
public class SkullKingPlayer {
    private String username;
    private List<SkullKingCard> hand = new ArrayList<>();
    private Integer bid;
    private int tricksWon;
    private int score;
    private int roundPoints;
    private SkullKingCard cardPlayed;

    /**
     * Default constructor.
     */
    public SkullKingPlayer() {
    }

    /**
     * Constructs a new SkullKingPlayer with the specified username.
     *
     * @param username The username of the player.
     */
    public SkullKingPlayer(String username) {
        this.username = username;
    }

    /**
     * Gets the username of the player.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the player.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the list of cards currently in the player's hand.
     *
     * @return The list of cards.
     */
    public List<SkullKingCard> getHand() {
        return hand;
    }

    /**
     * Sets the list of cards in the player's hand.
     *
     * @param hand The list of cards to set.
     */
    public void setHand(List<SkullKingCard> hand) {
        this.hand = hand;
    }

    /**
     * Gets the number of tricks bid by the player for the current round.
     *
     * @return The bid count, or null if not yet bid.
     */
    public Integer getBid() {
        return bid;
    }

    /**
     * Sets the number of tricks bid by the player.
     *
     * @param bid The bid count to set.
     */
    public void setBid(Integer bid) {
        this.bid = bid;
    }

    /**
     * Gets the number of tricks won by the player in the current round.
     *
     * @return The number of tricks won.
     */
    public int getTricksWon() {
        return tricksWon;
    }

    /**
     * Sets the number of tricks won by the player.
     *
     * @param tricksWon The number of tricks won to set.
     */
    public void setTricksWon(int tricksWon) {
        this.tricksWon = tricksWon;
    }

    /**
     * Gets the player's total cumulative score.
     *
     * @return The total score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the player's total cumulative score.
     *
     * @param score The total score to set.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Gets the points earned (or lost) by the player in the last completed round.
     *
     * @return The points delta.
     */
    public int getRoundPoints() {
        return roundPoints;
    }

    /**
     * Sets the points earned (or lost) by the player in the last round.
     *
     * @param roundPoints The points delta to set.
     */
    public void setRoundPoints(int roundPoints) {
        this.roundPoints = roundPoints;
    }

    /**
     * Gets the card currently played by the player in the active trick.
     *
     * @return The card played.
     */
    public SkullKingCard getCardPlayed() {
        return cardPlayed;
    }

    /**
     * Sets the card played by the player in the active trick.
     *
     * @param cardPlayed The card played to set.
     */
    public void setCardPlayed(SkullKingCard cardPlayed) {
        this.cardPlayed = cardPlayed;
    }
}
