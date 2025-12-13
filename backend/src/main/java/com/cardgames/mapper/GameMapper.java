package com.cardgames.mapper;

import com.cardgames.model.Game;
import com.cardgames.model.GamePlayer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface GameMapper {
    void insertGame(Game game);

    void insertGamePlayer(GamePlayer gamePlayer);

    Optional<Game> findByCode(String code);

    Optional<Game> findById(Long id);

    boolean isPlayerInGame(@Param("gameId") Long gameId, @Param("userId") Long userId);

    void updateGameStatus(@Param("gameId") Long gameId, @Param("status") String status);
}
