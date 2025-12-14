package com.cardgames.model.skullking;

public class SkullKingCard {
    private String id;
    private SkullKingCardType type;
    private SkullKingColor color;
    private int value; // 1-14 for NUMBER, 0 otherwise

    public SkullKingCard() {
    }

    public SkullKingCard(String id, SkullKingCardType type, SkullKingColor color, int value) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SkullKingCardType getType() {
        return type;
    }

    public void setType(SkullKingCardType type) {
        this.type = type;
    }

    public SkullKingColor getColor() {
        return color;
    }

    public void setColor(SkullKingColor color) {
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (type == SkullKingCardType.NUMBER) {
            return color + " " + value;
        }
        return type.toString();
    }
}
