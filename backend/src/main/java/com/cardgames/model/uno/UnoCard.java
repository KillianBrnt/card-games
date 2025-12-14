package com.cardgames.model.uno;

public class UnoCard {
    private String id;
    private UnoCardColor color;
    private UnoCardType type;
    private Integer value; // 0-9 for NUMBER cards
    private String displayValue; // For UI logic mostly, e.g. "+2", "Skip"

    public UnoCard() {
    }

    public UnoCard(String id, UnoCardColor color, UnoCardType type, Integer value, String displayValue) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.value = value;
        this.displayValue = displayValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UnoCardColor getColor() {
        return color;
    }

    public void setColor(UnoCardColor color) {
        this.color = color;
    }

    public UnoCardType getType() {
        return type;
    }

    public void setType(UnoCardType type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }
}
