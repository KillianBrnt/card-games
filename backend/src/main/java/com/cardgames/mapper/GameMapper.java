package com.cardgames.mapper;

import com.cardgames.model.Game;
import com.cardgames.model.GamePlayer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * Mapper interface for Game-related database operations.
 */
@Mapper
public interface GameMapper {

    /**
     * Inserts a new game into the database.
     *
     * @param game The game to insert.
     */
    void insertGame(Game game);

    /**
     * Inserts a new game player record into the database.
     *
     * @param gamePlayer The game player to insert.
     */
    void insertGamePlayer(GamePlayer gamePlayer);

    /**
     * Finds a game by its unique code.
     *
     * @param code The game code.
     * @return An Optional containing the game if found, or empty otherwise.
     */
    Optional<Game> findByCode(String code);

    /**
     * Finds a game by its ID.
     *
     * @param id The game ID.
     * @return An Optional containing the game if found, or empty otherwise.
     */
    Optional<Game> findById(Long id);

    /**
     * Checks if a user is currently a player in a specific game.
     *
     * @param gameId The ID of the game.
     * @param userId The ID of the user.
     * @return True if the user is in the game, false otherwise.
     */
    boolean isPlayerInGame(@Param("gameId") Long gameId, @Param("userId") Long userId);

    /**
     * Updates the status of a game.
     *
     * @param gameId The ID of the game.
     * @param status The new status.
     */
    void updateGameStatus(@Param("gameId") Long gameId, @Param("status") String status);
}
