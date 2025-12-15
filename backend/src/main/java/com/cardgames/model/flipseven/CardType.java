package com.cardgames.model.flipseven;

/**
 * Enumeration of the different types of cards in Flip Seven.
 */
public enum CardType {
    /**
     * Standard number card (0-9).
     */
    NUMBER,

    /**
     * Freeze card.
     */
    ACTION_FREEZE,

    /**
     * Flip 3 card.
     */
    ACTION_FLIP3,

    /**
     * Second chance card.
     */
    ACTION_SECOND_CHANCE,

    /**
     * Plus modifier card.
     */
    MODIFIER_PLUS,

    /**
     * Multiply modifier card.
     */
    MODIFIER_MULTIPLY
}
