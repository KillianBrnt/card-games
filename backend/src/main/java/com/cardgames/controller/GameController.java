package com.cardgames.controller;

import com.cardgames.dto.CreateGameRequest;
import com.cardgames.dto.GameResponse;
import com.cardgames.dto.JoinGameRequest;
import com.cardgames.model.User;
import com.cardgames.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/create")
    public ResponseEntity<GameResponse> createGame(@RequestBody CreateGameRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        GameResponse response = gameService.createGame(request.getGameType(), user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(@RequestBody JoinGameRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        GameResponse response = gameService.joinGame(request.getGameCode(), user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<GameResponse> getGameInfo(@RequestParam Long gameId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        GameResponse response = gameService.getGameInfo(gameId, user);
        return ResponseEntity.ok(response);
    }
}
