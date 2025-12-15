package com.cardgames.model.flipseven;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a player in a Flip Seven game session.
 */
public class FlipSevenPlayer {
    @JsonProperty("username")
    private String username;

    @JsonProperty("hand")
    private List<Card> hand = new ArrayList<>();

    @JsonProperty("bankedCards")
    private List<Card> bankedCards = new ArrayList<>();

    @JsonProperty("roundScore")
    private int roundScore;

    @JsonProperty("totalScore")
    private int totalScore;

    @JsonProperty("isRoundActive")
    private boolean isRoundActive;

    @JsonProperty("hasSecondChance")
    private boolean hasSecondChance;

    @JsonProperty("lastRoundScore")
    private int lastRoundScore;

    /**
     * Default constructor.
     */
    public FlipSevenPlayer() {
    }

    /**
     * Constructs a new FlipSevenPlayer with the specified username.
     *
     * @param username The username of the player.
     */
    public FlipSevenPlayer(String username) {
        this.username = username;
        this.isRoundActive = true;
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
     * Gets the list of cards currently in the player's hand for this round.
     *
     * @return The list of cards in hand.
     */
    public List<Card> getHand() {
        return hand;
    }

    /**
     * Sets the list of cards in the player's hand.
     *
     * @param hand The list of cards to set.
     */
    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    /**
     * Gets the list of cards banked by the player.
     *
     * @return The list of banked cards.
     */
    public List<Card> getBankedCards() {
        return bankedCards;
    }

    /**
     * Sets the list of cards banked by the player.
     *
     * @param bankedCards The list of banked cards to set.
     */
    public void setBankedCards(List<Card> bankedCards) {
        this.bankedCards = bankedCards;
    }

    /**
     * Gets the player's score for the current round.
     *
     * @return The round score.
     */
    public int getRoundScore() {
        return roundScore;
    }

    /**
     * Sets the player's score for the current round.
     *
     * @param roundScore The round score to set.
     */
    public void setRoundScore(int roundScore) {
        this.roundScore = roundScore;
    }

    /**
     * Gets the player's total cumulative score.
     *
     * @return The total score.
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Sets the player's total cumulative score.
     *
     * @param totalScore The total score to set.
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * Checks if the player is currently active in the round (has not busted or
     * stayed).
     *
     * @return True if active, false otherwise.
     */
    public boolean isRoundActive() {
        return isRoundActive;
    }

    /**
     * Sets the player's active status for the round.
     *
     * @param roundActive True if active, false otherwise.
     */
    public void setRoundActive(boolean roundActive) {
        isRoundActive = roundActive;
    }

    /**
     * Checks if the player has a generic second chance modifier active.
     *
     * @return True if second chance is active, false otherwise.
     */
    public boolean isHasSecondChance() {
        return hasSecondChance;
    }

    /**
     * Sets whether the player has a second chance modifier active.
     *
     * @param hasSecondChance True to activate, false to deactivate.
     */
    public void setHasSecondChance(boolean hasSecondChance) {
        this.hasSecondChance = hasSecondChance;
    }

    /**
     * Gets the score the player achieved in the last round.
     *
     * @return The last round score.
     */
    public int getLastRoundScore() {
        return lastRoundScore;
    }

    /**
     * Sets the score the player achieved in the last round.
     *
     * @param lastRoundScore The last round score to set.
     */
    public void setLastRoundScore(int lastRoundScore) {
        this.lastRoundScore = lastRoundScore;
    }
}
