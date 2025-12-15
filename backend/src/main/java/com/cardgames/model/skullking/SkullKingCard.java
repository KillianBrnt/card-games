package com.cardgames.model.skullking;

/**
 * Represents a playing card in the Skull King game.
 */
public class SkullKingCard {
    private String id;
    private SkullKingCardType type;
    private SkullKingColor color;
    private int value;

    /**
     * Default constructor.
     */
    public SkullKingCard() {
    }

    /**
     * Constructs a new SkullKingCard with the specified attributes.
     *
     * @param id    The unique identifier for the card.
     * @param type  The type of the card (e.g., NUMBER, PIRATE).
     * @param color The suit/color of the card (e.g., RED, BLACK).
     * @param value The face value of the card (1-14 for NUMBER cards).
     */
    public SkullKingCard(String id, SkullKingCardType type, SkullKingColor color, int value) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.value = value;
    }

    /**
     * Gets the unique identifier of the card.
     *
     * @return The card ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the card.
     *
     * @param id The card ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the type of the card.
     *
     * @return The card type.
     */
    public SkullKingCardType getType() {
        return type;
    }

    /**
     * Sets the type of the card.
     *
     * @param type The card type to set.
     */
    public void setType(SkullKingCardType type) {
        this.type = type;
    }

    /**
     * Gets the color or suit of the card.
     *
     * @return The card color.
     */
    public SkullKingColor getColor() {
        return color;
    }

    /**
     * Sets the color or suit of the card.
     *
     * @param color The card color to set.
     */
    public void setColor(SkullKingColor color) {
        this.color = color;
    }

    /**
     * Gets the numerical value of the card.
     *
     * @return The card value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the numerical value of the card.
     *
     * @param value The card value to set.
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Returns a string representation of the card.
     *
     * @return The string representation (e.g., "RED 5" or "PIRATE").
     */
    @Override
    public String toString() {
        if (type == SkullKingCardType.NUMBER) {
            return color + " " + value;
        }
        return type.toString();
    }
}
