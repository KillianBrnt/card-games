package com.cardgames.model.uno;

/**
 * Enumeration of the different types of Uno cards.
 */
public enum UnoCardType {
    /**
     * Number card (0-9).
     */
    NUMBER,

    /**
     * Skip card (skips the next player's turn).
     */
    SKIP,

    /**
     * Reverse card (reverses the direction of play).
     */
    REVERSE,

    /**
     * Draw Two card (next player draws 2 cards).
     */
    DRAW_TWO,

    /**
     * Wild card (player chooses the color).
     */
    WILD,

    /**
     * Wild Draw Four card (player chooses color, next player draws 4).
     */
    WILD_DRAW_FOUR
}
