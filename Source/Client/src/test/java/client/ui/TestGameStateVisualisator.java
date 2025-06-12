package client.ui;

import client.gamedata.GameStateTracker;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TestGameStateVisualisator {

    @Test
    void givenGameStateChanged_whenDisplayIsCalled_thenOutputContainsNewState() {
        GameStateTracker tracker = new GameStateTracker();
        GameStateVisualisator visualisator = new GameStateVisualisator(tracker);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        tracker.updateGameState(client.gamedata.EGameState.WON);

        System.setOut(originalOut); 
        assertTrue(outContent.toString().contains("Game state changed: WON"));
    }
}
