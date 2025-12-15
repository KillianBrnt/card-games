package com.cardgames.model;

import java.time.LocalDateTime;

/**
 * Represents a game instance in the system.
 */
public class Game {
    private Long id;
    private String code;
    private String type;
    private Long hostUserId;
    private GameStatus status;
    private LocalDateTime createdAt;

    /**
     * Gets the unique identifier of the game.
     *
     * @return The game ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the game.
     *
     * @param id The game ID to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the unique code used for joining the game.
     *
     * @return The game code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the unique code for the game.
     *
     * @param code The game code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the type of the game (e.g., "SKULL_KING", "UNO").
     *
     * @return The game type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the game.
     *
     * @param type The game type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the user ID of the game host.
     *
     * @return The host's user ID.
     */
    public Long getHostUserId() {
        return hostUserId;
    }

    /**
     * Sets the user ID of the game host.
     *
     * @param hostUserId The host's user ID to set.
     */
    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }

    /**
     * Gets the current status of the game.
     *
     * @return The game status.
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of the game.
     *
     * @param status The game status to set.
     */
    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * Gets the timestamp when the game was created.
     *
     * @return The creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the game was created.
     *
     * @param createdAt The creation timestamp to set.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
