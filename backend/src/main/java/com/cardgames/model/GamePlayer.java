package com.cardgames.model;

import java.time.LocalDateTime;

/**
 * Represents a player participating in a game instance.
 */
public class GamePlayer {
    private Long id;
    private Long gameId;
    private Long userId;
    private String displayName;
    private LocalDateTime joinedAt;
    private boolean isHost;

    /**
     * Gets the unique identifier of the game player record.
     *
     * @return The player record ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the game player record.
     *
     * @param id The player record ID to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the ID of the game this player is associated with.
     *
     * @return The game ID.
     */
    public Long getGameId() {
        return gameId;
    }

    /**
     * Sets the ID of the game this player is associated with.
     *
     * @param gameId The game ID to set.
     */
    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the user ID of the player.
     *
     * @return The user ID.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user ID of the player.
     *
     * @param userId The user ID to set.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Gets the display name of the player in the game context.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the player in the game context.
     *
     * @param displayName The display name to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the timestamp when the player joined the game.
     *
     * @return The join timestamp.
     */
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    /**
     * Sets the timestamp when the player joined the game.
     *
     * @param joinedAt The join timestamp to set.
     */
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * Checks if the player is the host of the game.
     *
     * @return True if the player is the host, false otherwise.
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Sets whether the player is the host of the game.
     *
     * @param host True to set as host, false otherwise.
     */
    public void setHost(boolean host) {
        isHost = host;
    }
}
