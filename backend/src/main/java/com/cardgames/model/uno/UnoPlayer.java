package com.cardgames.model.uno;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in an Uno game session.
 */
public class UnoPlayer {
    private String username;
    private List<UnoCard> hand = new ArrayList<>();
    private boolean isRoundActive = true;
    private boolean saidUno = false;

    /**
     * Default constructor.
     */
    public UnoPlayer() {
    }

    /**
     * Constructs a new UnoPlayer with the specified username.
     *
     * @param username The username of the player.
     */
    public UnoPlayer(String username) {
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
    public List<UnoCard> getHand() {
        return hand;
    }

    /**
     * Sets the list of cards in the player's hand.
     *
     * @param hand The list of cards to set.
     */
    public void setHand(List<UnoCard> hand) {
        this.hand = hand;
    }

    /**
     * Checks if the player is currently active in the round.
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
     * Checks if the player has declared "Uno".
     *
     * @return True if "Uno" has been said, false otherwise.
     */
    public boolean hasSaidUno() {
        return saidUno;
    }

    /**
     * Sets whether the player has declared "Uno".
     *
     * @param saidUno True if "Uno" declared, false otherwise.
     */
    public void setSaidUno(boolean saidUno) {
        this.saidUno = saidUno;
    }
}
