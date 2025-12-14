package com.cardgames.engine;

import com.cardgames.websocket.model.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class GameEngineHandlerTest {

    @Mock
    private FlipSevenGameEngine flipSevenGameEngine;

    private GameEngineHandler gameEngineHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameEngineHandler = new GameEngineHandler(flipSevenGameEngine);
    }

    @Test
    public void testHandleAction_FlipSeven() {
        Action action = new Action();
        action.setGameType("FLIP_SEVEN");

        gameEngineHandler.handleAction(action);

        verify(flipSevenGameEngine, times(1)).handleAction(action);
    }

    @Test
    public void testHandleAction_UnknownType_Default() {
        // If only one engine, it might default (based on current implementation logic)
        Action action = new Action();
        action.setGameType(null); // Unknown

        gameEngineHandler.handleAction(action);

        // Implementation says: if size==1 and type==null, use default.
        verify(flipSevenGameEngine, times(1)).handleAction(action);
    }

    @Test
    public void testInitializeGame_FlipSeven() {
        Long gameId = 1L;
        gameEngineHandler.initializeGame("FLIP_SEVEN", gameId);

        verify(flipSevenGameEngine, times(1)).initializeGame(gameId);
    }

    @Test
    public void testInitializeGame_Unknown() {
        Long gameId = 1L;
        gameEngineHandler.initializeGame("POKER", gameId);

        verify(flipSevenGameEngine, never()).initializeGame(anyLong());
    }
}
