package com.cardgames.dto;

/**
 * Data Transfer Object for creating a new game.
 */
public class CreateGameRequest {
    private String gameType;

    /**
     * Gets the type of game to create.
     *
     * @return The game type (e.g., "SKULL_KING", "FLIP_SEVEN", "UNO").
     */
    public String getGameType() {
        return gameType;
    }

    /**
     * Sets the type of game to create.
     *
     * @param gameType The game type to set.
     */
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
}
