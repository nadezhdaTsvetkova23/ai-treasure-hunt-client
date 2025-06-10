package client.gamedata;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GameInfo {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private String phase = "Treasure Hunt";
    private int turn = 1;
    private String move = "";
    private String myPosition = "";
    private String enemyPosition = "";
    private String treasureFound = "";
    private String status = ""; // WIN/LOSE

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }
    
    public String getPhase() { return phase; }
    
    public void setPhase(String phase) {
        String old = this.phase; this.phase = phase;
        changes.firePropertyChange("phase", old, phase);
    }
    
    public int getTurn() { return turn; }
    
    public void setTurn(int turn) {
        int old = this.turn; this.turn = turn;
        changes.firePropertyChange("turn", old, turn);
    }
    
    public String getMove() { return move; }
    
    public void setMove(String move) {
        String old = this.move; this.move = move;
        changes.firePropertyChange("move", old, move);
    }
    
    public String getMyPosition() { return myPosition; }
    
    public void setMyPosition(String pos) {
        String old = this.myPosition; this.myPosition = pos;
        changes.firePropertyChange("myPosition", old, pos);
    }
    
    public String getEnemyPosition() { return enemyPosition; }
    
    public void setEnemyPosition(String pos) {
        String old = this.enemyPosition; this.enemyPosition = pos;
        changes.firePropertyChange("enemyPosition", old, pos);
    }
    
    public String getTreasureFound() { return treasureFound; }
    
    public void setTreasureFound(String found) {
        String old = this.treasureFound; this.treasureFound = found;
        changes.firePropertyChange("treasureFound", old, found);
    }
    
    public String getStatus() { return status; }
    
    public void setStatus(String status) {
        String old = this.status; this.status = status;
        changes.firePropertyChange("status", old, status);
    }
    

    public void printGameInfoCLI() {
        System.out.println(
            "Phase: " + getPhase() + "\n"
            + "Turn: " + getTurn() + "\n"
            + "Move: " + getMove() + "\n"
            + "My position: " + getMyPosition() + "\n"
            + "Enemy position: " + getEnemyPosition() + "\n"
            + "Treasure found on position: " + getTreasureFound() + "\n"
            + (getStatus() != null && !getStatus().isEmpty() ? ("Status: " + getStatus()) : "")
        );
    }
}
