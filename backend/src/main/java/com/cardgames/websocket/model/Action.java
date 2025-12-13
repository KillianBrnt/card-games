package com.cardgames.websocket.model;

import java.util.Map;

public class Action {
    private Map<String, Object> payload;
    private String sender;
    private Long gameId;
    private ActionType type;
    private String gameType;

    public enum ActionType {
        CHAT,
        JOIN,
        LEAVE,
        GAME_ACTION,
        SYSTEM,
        PING,
        SYNC_REQUEST
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
}
