package com.cardgames.model;

/**
 * Represents the various statuses a game can be in.
 */
public enum GameStatus {
    /**
     * The game is waiting for players to join.
     */
    WAITING,

    /**
     * The game is currently in progress.
     */
    PLAYING,

    /**
     * The game has concluded.
     */
    FINISHED
}
