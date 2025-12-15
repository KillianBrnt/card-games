package com.cardgames.dto;

/**
 * Data Transfer Object for joining an existing game.
 */
public class JoinGameRequest {
    private String gameCode;

    /**
     * Gets the code of the game to join.
     *
     * @return The game code.
     */
    public String getGameCode() {
        return gameCode;
    }

    /**
     * Sets the code of the game to join.
     *
     * @param gameCode The game code to set.
     */
    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }
}
