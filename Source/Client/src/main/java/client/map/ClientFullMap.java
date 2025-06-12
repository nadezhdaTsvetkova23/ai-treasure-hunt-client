package client.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import messagesbase.messagesfromserver.EPlayerPositionState;

public class ClientFullMap {

    private final HalfMap myPlayerHalfMap;
    private final HalfMap enemyPlayerHalfMap;
    private final int width;
    private final int height;

    public ClientFullMap(HalfMap myPlayerHalfMap, HalfMap enemyPlayerHalfMap, int width, int height) {
        this.myPlayerHalfMap = myPlayerHalfMap;
        this.enemyPlayerHalfMap = enemyPlayerHalfMap;
        this.width = width;
        this.height = height;
    }

    public HalfMap getMyPlayerHalfMap() {
        return myPlayerHalfMap;
    }
    
    public Map<Coordinate, Field> getMyFields() {
        return myPlayerHalfMap.getFields();
    }

	public Map<Coordinate, Field> getEnemyFields() {
		return enemyPlayerHalfMap.getFields();
	}
	
    public HalfMap getEnemyPlayerHalfMap() {
        return enemyPlayerHalfMap;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Map<Coordinate, Field> getAllFields() {
        Map<Coordinate, Field> combined = new HashMap<>();
        combined.putAll(myPlayerHalfMap.getFields());
        combined.putAll(enemyPlayerHalfMap.getFields());
        return combined;
    }

    public Field getFieldAt(Coordinate coordinate) {
        return getAllFields().get(coordinate);
    }

    public boolean isComplete() {
        return getAllFields().size() == width * height;
    }
    
    public Optional<Coordinate> findMyPlayerPosition() {
        for (Map.Entry<Coordinate, Field> entry : getAllFields().entrySet()) {
            if (entry.getValue().getPlayerPresence() == EPlayerPresence.MY_PLAYER) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    public boolean isValidHalfMapSize() {
        return myPlayerHalfMap.getFields().size() == 50 && enemyPlayerHalfMap.getFields().size() == 50;
    }

}
