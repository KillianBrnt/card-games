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

    @Mock
    private UnoGameEngine unoGameEngine;

    @Mock
    private SkullKingGameEngine skullKingGameEngine;

    private GameEngineHandler gameEngineHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameEngineHandler = new GameEngineHandler(flipSevenGameEngine, unoGameEngine, skullKingGameEngine);
    }

    @Test
    public void testHandleAction_FlipSeven() {
        Action action = new Action();
        action.setGameType("FLIP_SEVEN");

        gameEngineHandler.handleAction(action);

        verify(flipSevenGameEngine, times(1)).handleAction(action);
        verify(unoGameEngine, never()).handleAction(action);
    }

    @Test
    public void testHandleAction_Uno() {
        Action action = new Action();
        action.setGameType("UNO");

        gameEngineHandler.handleAction(action);

        verify(unoGameEngine, times(1)).handleAction(action);
        verify(flipSevenGameEngine, never()).handleAction(action);
    }

    @Test
    public void testHandleAction_UnknownType_NoDefault_WhenMultipleEngines() {
        // Since we have 2 engines now, it should NOT default to FlipSeven
        Action action = new Action();
        action.setGameType(null); // Unknown

        gameEngineHandler.handleAction(action);

        verify(flipSevenGameEngine, never()).handleAction(action);
        verify(unoGameEngine, never()).handleAction(action);
    }

    @Test
    public void testInitializeGame_FlipSeven() {
        Long gameId = 1L;
        gameEngineHandler.initializeGame("FLIP_SEVEN", gameId);

        verify(flipSevenGameEngine, times(1)).initializeGame(gameId);
    }

    @Test
    public void testInitializeGame_Uno() {
        Long gameId = 1L;
        gameEngineHandler.initializeGame("UNO", gameId);

        verify(unoGameEngine, times(1)).initializeGame(gameId);
    }

    @Test
    public void testInitializeGame_SkullKing() {
        Long gameId = 1L;
        gameEngineHandler.initializeGame("SKULL_KING", gameId);

        verify(skullKingGameEngine, times(1)).initializeGame(gameId);
    }

    @Test
    public void testInitializeGame_Unknown() {
        Long gameId = 1L;
        gameEngineHandler.initializeGame("POKER", gameId);

        verify(flipSevenGameEngine, never()).initializeGame(anyLong());
        verify(unoGameEngine, never()).initializeGame(anyLong());
    }
}
