package client.gamedata;

public class GameStateTracker {
    private EGameState gameState = EGameState.MUST_ACT;
    private boolean hasTreasure = false;
    private boolean seenTreasure = false;

    public EGameState getGameState() {
        return gameState;
    }

    public void updateGameState(EGameState newState) {
        this.gameState = newState;
    }

    public boolean hasTreasure() {
        return hasTreasure;
    }

    public void setHasTreasure(boolean value) {
        this.hasTreasure = value;
    }

    public boolean hasSeenTreasure() {
        return seenTreasure;
    }

    public void setSeenTreasure(boolean seen) {
        this.seenTreasure = seen;
    }
}
