package com.cardgames.service;

import com.cardgames.dto.GameResponse;
import com.cardgames.mapper.GameMapper;
import com.cardgames.model.Game;
import com.cardgames.model.GamePlayer;
import com.cardgames.model.GameStatus;
import com.cardgames.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cardgames.model.exception.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GameService {

    private final GameMapper gameMapper;

    public GameService(GameMapper gameMapper) {
        this.gameMapper = gameMapper;
    }

    @Transactional
    public GameResponse createGame(String gameType, User user) {
        String code = generateGameCode();

        Game game = new Game();
        game.setCode(code);
        game.setType(gameType);
        game.setHostUserId(user.getId());
        game.setStatus(GameStatus.WAITING);
        game.setCreatedAt(LocalDateTime.now());

        gameMapper.insertGame(game);

        GamePlayer host = new GamePlayer();
        host.setGameId(game.getId());
        host.setUserId(user.getId());
        host.setDisplayName(user.getUsername());
        host.setJoinedAt(LocalDateTime.now());
        host.setHost(true);

        gameMapper.insertGamePlayer(host);

        return new GameResponse(game.getId(), game.getCode(), game.getStatus());
    }

    @Transactional
    public GameResponse joinGame(String gameCode, User user) {
        Game game = gameMapper.findByCode(gameCode)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (gameMapper.isPlayerInGame(game.getId(), user.getId())) {
            return new GameResponse(game.getId(), game.getCode(), game.getStatus());
        }

        if (game.getStatus() != GameStatus.WAITING) {
            throw new RuntimeException("Game is not open for joining");
        }

        GamePlayer player = new GamePlayer();
        player.setGameId(game.getId());
        player.setUserId(user.getId());
        player.setDisplayName(user.getUsername());
        player.setJoinedAt(LocalDateTime.now());
        player.setHost(false);

        gameMapper.insertGamePlayer(player);

        return new GameResponse(game.getId(), game.getCode(), game.getStatus());
    }

    public GameResponse getGameInfo(Long gameId, User user) {
        if (!gameMapper.isPlayerInGame(gameId, user.getId())) {
            throw new AccessDeniedException("You are not part of this game");
        }

        Game game = gameMapper.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        return new GameResponse(game.getId(), game.getCode(), game.getStatus());
    }

    private String generateGameCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
