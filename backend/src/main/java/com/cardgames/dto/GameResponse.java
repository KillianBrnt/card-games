package com.cardgames.dto;

import com.cardgames.model.GameStatus;

public class GameResponse {
    private Long gameId;
    private String gameCode;
    private GameStatus status;

    public GameResponse() {
    }

    public GameResponse(Long gameId, String gameCode, GameStatus status) {
        this.gameId = gameId;
        this.gameCode = gameCode;
        this.status = status;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}
