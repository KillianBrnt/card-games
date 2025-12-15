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

    /**
     * Creates a new game instance.
     *
     * @param request The request body containing the game type.
     * @return A ResponseEntity containing the created game details.
     */
    @PostMapping("/create")
    public ResponseEntity<GameResponse> createGame(@RequestBody CreateGameRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        GameResponse response = gameService.createGame(request.getGameType(), user);
        return ResponseEntity.ok(response);
    }

    /**
     * Joins an existing game using a game code.
     *
     * @param request The request body containing the game code.
     * @return A ResponseEntity containing the joined game details.
     */
    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(@RequestBody JoinGameRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        GameResponse response = gameService.joinGame(request.getGameCode(), user);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves information about a specific game.
     *
     * @param gameId The ID of the game to retrieve.
     * @return A ResponseEntity containing the game details.
     */
    @GetMapping("/info")
    public ResponseEntity<GameResponse> getGameInfo(@RequestParam Long gameId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        GameResponse response = gameService.getGameInfo(gameId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Starts a game session. Only the host can perform this action.
     *
     * @param gameId The ID of the game to start.
     * @return A standard empty ResponseEntity indicating success.
     */
    @PostMapping("/{gameId}/start")
    public ResponseEntity<Void> startGame(@PathVariable Long gameId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        gameService.startGame(gameId, user);
        return ResponseEntity.ok().build();
    }
}
