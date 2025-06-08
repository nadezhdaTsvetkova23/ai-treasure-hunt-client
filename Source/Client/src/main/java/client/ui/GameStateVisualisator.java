package client.ui;

import client.gamedata.GameStateTracker;

public class GameStateVisualisator {
    public GameStateVisualisator(GameStateTracker model) {
        model.addPropertyChangeListener(evt -> {
            if ("gameState".equals(evt.getPropertyName())) {
                displayGameState("Game state changed: " + evt.getNewValue());
            }
            if ("hasTreasure".equals(evt.getPropertyName())) {
                displayGameState("Treasure collected: " + evt.getNewValue());
            }
        });
    }

    private void displayGameState(String msg) {
        System.out.println("[Game State] " + msg);
    }
}
