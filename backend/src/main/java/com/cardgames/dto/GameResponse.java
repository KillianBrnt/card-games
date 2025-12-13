package com.cardgames.dto;

import com.cardgames.model.GameStatus;

public class GameResponse {
    private Long gameId;
    private String gameCode;
    private GameStatus status;

    private Long hostUserId;

    public GameResponse() {
    }

    public GameResponse(Long gameId, String gameCode, GameStatus status, Long hostUserId) {
        this.gameId = gameId;
        this.gameCode = gameCode;
        this.status = status;
        this.hostUserId = hostUserId;
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

    public Long getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }
}
