package com.cardgames.dto;

import com.cardgames.model.GameStatus;

/**
 * Data Transfer Object for returning game details.
 */
public class GameResponse {
    private Long gameId;
    private String gameCode;
    private GameStatus status;

    private String gameType;
    private Long hostUserId;

    /**
     * Default constructor.
     */
    public GameResponse() {
    }

    /**
     * Constructs a new GameResponse with the specified details.
     *
     * @param gameId     The unique identifier of the game.
     * @param gameCode   The secret code to join the game.
     * @param status     The current status of the game.
     * @param gameType   The type of the game.
     * @param hostUserId The ID of the user who is the host.
     */
    public GameResponse(Long gameId, String gameCode, GameStatus status, String gameType, Long hostUserId) {
        this.gameId = gameId;
        this.gameCode = gameCode;
        this.status = status;
        this.gameType = gameType;
        this.hostUserId = hostUserId;
    }

    /**
     * Gets the game ID.
     *
     * @return The game ID.
     */
    public Long getGameId() {
        return gameId;
    }

    /**
     * Sets the game ID.
     *
     * @param gameId The game ID to set.
     */
    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the game code.
     *
     * @return The game code.
     */
    public String getGameCode() {
        return gameCode;
    }

    /**
     * Sets the game code.
     *
     * @param gameCode The game code to set.
     */
    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    /**
     * Gets the game status.
     *
     * @return The game status.
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * Sets the game status.
     *
     * @param status The game status to set.
     */
    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * Gets the game type.
     *
     * @return The game type.
     */
    public String getGameType() {
        return gameType;
    }

    /**
     * Sets the game type.
     *
     * @param gameType The game type to set.
     */
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    /**
     * Gets the host user ID.
     *
     * @return The host user ID.
     */
    public Long getHostUserId() {
        return hostUserId;
    }

    /**
     * Sets the host user ID.
     *
     * @param hostUserId The host user ID to set.
     */
    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }
}
