package com.cardgames.model.flipseven;

public class Card {
    private String id; // Unique ID for frontend keys (e.g. "5-2" for the second '5' card)
    private CardType type;
    private int value; // For NUMBER and MODIFIER_PLUS
    private String name; // Display name

    public Card() {
    }

    public Card(String id, CardType type, int value, String name) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
