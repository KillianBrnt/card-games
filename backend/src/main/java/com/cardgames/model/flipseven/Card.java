package com.cardgames.model.flipseven;

/**
 * Represents a single card in the Flip Seven game.
 */
public class Card {
    private String id;
    private CardType type;
    private int value;
    private String name;
    private boolean noEffect;

    /**
     * Default constructor.
     */
    public Card() {
    }

    /**
     * Constructs a new Card with the specified attributes.
     *
     * @param id    The unique identifier for the card.
     * @param type  The type of the card.
     * @param value The numerical value associated with the card.
     * @param name  The display name of the card.
     */
    public Card(String id, CardType type, int value, String name) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.name = name;
    }

    /**
     * Gets the unique identifier of the card (e.g., "5-2").
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
    public CardType getType() {
        return type;
    }

    /**
     * Sets the type of the card.
     *
     * @param type The card type to set.
     */
    public void setType(CardType type) {
        this.type = type;
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
     * Gets the display name of the card.
     *
     * @return The card name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the card.
     *
     * @param name The card name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if the card has no effect in the current context.
     *
     * @return True if the card has no effect, false otherwise.
     */
    public boolean isNoEffect() {
        return noEffect;
    }

    /**
     * Sets whether the card has no effect.
     *
     * @param noEffect True to set no effect, false otherwise.
     */
    public void setNoEffect(boolean noEffect) {
        this.noEffect = noEffect;
    }

    /**
     * Returns a string representation of the card, which is its name.
     *
     * @return The card name.
     */
    @Override
    public String toString() {
        return name;
    }
}
