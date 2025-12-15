package com.cardgames.model.uno;

/**
 * Represents a playing card in the Uno game.
 */
public class UnoCard {
    private String id;
    private UnoCardColor color;
    private UnoCardType type;
    private Integer value;
    private String displayValue;

    /**
     * Default constructor.
     */
    public UnoCard() {
    }

    /**
     * Constructs a new UnoCard with the specified attributes.
     *
     * @param id           The unique identifier for the card.
     * @param color        The color of the card (e.g., RED, WILD).
     * @param type         The type of the card (e.g., NUMBER, SKIP).
     * @param value        The face value (0-9 for NUMBER cards).
     * @param displayValue The string representation for UI (e.g., "+2", "Skip").
     */
    public UnoCard(String id, UnoCardColor color, UnoCardType type, Integer value, String displayValue) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.value = value;
        this.displayValue = displayValue;
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
     * Gets the color of the card.
     *
     * @return The card color.
     */
    public UnoCardColor getColor() {
        return color;
    }

    /**
     * Sets the color of the card.
     *
     * @param color The card color to set.
     */
    public void setColor(UnoCardColor color) {
        this.color = color;
    }

    /**
     * Gets the type of the card.
     *
     * @return The card type.
     */
    public UnoCardType getType() {
        return type;
    }

    /**
     * Sets the type of the card.
     *
     * @param type The card type to set.
     */
    public void setType(UnoCardType type) {
        this.type = type;
    }

    /**
     * Gets the numerical value of the card.
     *
     * @return The card value, or null for non-number cards.
     */
    public Integer getValue() {
        return value;
    }

    /**
     * Sets the numerical value of the card.
     *
     * @param value The card value to set.
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Gets the display value string for the card.
     *
     * @return The display value.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Sets the display value string for the card.
     *
     * @param displayValue The display value to set.
     */
    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }
}
