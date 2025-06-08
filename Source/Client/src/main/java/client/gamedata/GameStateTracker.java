package client.gamedata;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GameStateTracker {
    private EGameState gameState = EGameState.MUST_ACT;
    private boolean hasTreasure = false;
    private boolean seenTreasure = false;
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public EGameState getGameState() {
        return gameState;
    }

    public void updateGameState(EGameState newState) {
        EGameState oldState = this.gameState;
        this.gameState = newState;
        changes.firePropertyChange("gameState", oldState, newState);
    }

    public boolean hasTreasure() {
        return hasTreasure;
    }

    public void setHasTreasure(boolean value) {
        boolean oldValue = this.hasTreasure;
        this.hasTreasure = value;
        changes.firePropertyChange("hasTreasure", oldValue, value);
    }

    public boolean hasSeenTreasure() {
        return seenTreasure;
    }

    public void setSeenTreasure(boolean seen) {
        boolean old = this.seenTreasure;
        this.seenTreasure = seen;
        changes.firePropertyChange("seenTreasure", old, seen);
    }
}
