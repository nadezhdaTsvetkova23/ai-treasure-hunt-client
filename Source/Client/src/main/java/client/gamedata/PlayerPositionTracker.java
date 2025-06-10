package client.gamedata;

import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.Field;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerPositionTracker {
	private static final Logger log = LoggerFactory.getLogger(PlayerPositionTracker.class);
	
    private Coordinate myPlayerPosition;
    private Coordinate enemyPlayerPosition;

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void updatePositions(ClientFullMap fullMap) {
        log.trace("Scanning full map to update positions.");
        Optional<Coordinate> myPos = findMyPosition(fullMap);
        Optional<Coordinate> enemyPos = findEnemyPosition(fullMap);
        myPos.ifPresent(this::setMyPlayerPosition);
        enemyPos.ifPresent(this::setEnemyPlayerPosition);
    }

    public Optional<Coordinate> findMyPosition(ClientFullMap fullMap) {
        for (Map.Entry<Coordinate, Field> entry : fullMap.getAllFields().entrySet()) {
            if (entry.getValue().isMyPlayerHere()) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public Optional<Coordinate> findEnemyPosition(ClientFullMap fullMap) {
        for (Map.Entry<Coordinate, Field> entry : fullMap.getAllFields().entrySet()) {
            if (entry.getValue().isEnemyPlayerHere()) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public Coordinate getMyPlayerPosition() {
        return myPlayerPosition;
    }

    public void setMyPlayerPosition(Coordinate pos) {
        Coordinate old = this.myPlayerPosition;
        this.myPlayerPosition = pos;
        log.debug("Updated my player position: {}", pos);
        changes.firePropertyChange("myPlayerPosition", old, pos);
    }

    public Coordinate getEnemyPlayerPosition() {
        return enemyPlayerPosition;
    }

    public void setEnemyPlayerPosition(Coordinate pos) {
        Coordinate old = this.enemyPlayerPosition;
        this.enemyPlayerPosition = pos;
        log.debug("Updated enemy player position: {}", pos);
        changes.firePropertyChange("enemyPlayerPosition", old, pos);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }
}
