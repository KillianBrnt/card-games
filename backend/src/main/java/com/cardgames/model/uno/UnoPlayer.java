package com.cardgames.model.uno;

import java.util.ArrayList;
import java.util.List;

public class UnoPlayer {
    private String username;
    private List<UnoCard> hand = new ArrayList<>();
    private boolean isRoundActive = true;
    private boolean saidUno = false; // "UNO!" declared

    public UnoPlayer() {
    }

    public UnoPlayer(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<UnoCard> getHand() {
        return hand;
    }

    public void setHand(List<UnoCard> hand) {
        this.hand = hand;
    }

    public boolean isRoundActive() {
        return isRoundActive;
    }

    public void setRoundActive(boolean roundActive) {
        isRoundActive = roundActive;
    }

    public boolean hasSaidUno() {
        return saidUno;
    }

    public void setSaidUno(boolean saidUno) {
        this.saidUno = saidUno;
    }
}
