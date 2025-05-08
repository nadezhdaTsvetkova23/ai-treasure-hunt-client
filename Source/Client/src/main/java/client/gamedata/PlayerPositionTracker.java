package client.gamedata;

import client.map.Coordinate;

public class PlayerPositionTracker {
    private Coordinate myPlayerPosition;
    private Coordinate enemyPlayerPosition;

    public Coordinate getMyPlayerPosition() {
        return myPlayerPosition;
    }

    public void setMyPlayerPosition(Coordinate pos) {
        this.myPlayerPosition = pos;
    }

    public Coordinate getEnemyPlayerPosition() {
        return enemyPlayerPosition;
    }

    public void setEnemyPlayerPosition(Coordinate pos) {
        this.enemyPlayerPosition = pos;
    }
}
