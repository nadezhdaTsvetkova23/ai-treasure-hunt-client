package client.gamedata;

import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;
import client.map.EPlayerPresence;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TestPlayerPositionTracker {

    @Test
    void givenPlayerPositionTracker_whenSetMyPlayerPosition_thenListenerIsNotified() {
        PlayerPositionTracker tracker = new PlayerPositionTracker();
        AtomicReference<Coordinate> ref = new AtomicReference<>();
        tracker.addPropertyChangeListener(evt -> {
            if ("myPlayerPosition".equals(evt.getPropertyName())) {
                ref.set((Coordinate) evt.getNewValue());
            }
        });
        Coordinate pos = new Coordinate(2, 2);
        tracker.setMyPlayerPosition(pos);
        assertEquals(pos, tracker.getMyPlayerPosition());
        assertEquals(pos, ref.get());
    }

    @Test
    void givenFullMap_whenUpdatePositions_thenMyAndEnemyPositionsAreSet() {
        Map<Coordinate, Field> fields = new HashMap<>();
        Coordinate myCoord = new Coordinate(1, 1);
        Coordinate enemyCoord = new Coordinate(2, 2);
        fields.put(myCoord, new Field(myCoord, EGameTerrain.GRASS, null, null, EPlayerPresence.MY_PLAYER, false));
        fields.put(enemyCoord, new Field(enemyCoord, EGameTerrain.GRASS, null, null, EPlayerPresence.ENEMY_PLAYER, false));
        ClientFullMap fullMap = org.mockito.Mockito.mock(ClientFullMap.class);
        org.mockito.Mockito.when(fullMap.getAllFields()).thenReturn(fields);

        PlayerPositionTracker tracker = new PlayerPositionTracker();
        tracker.updatePositions(fullMap);

        assertEquals(myCoord, tracker.getMyPlayerPosition());
        assertEquals(enemyCoord, tracker.getEnemyPlayerPosition());
    }

    @Test
    void givenFullMap_whenFindMyPosition_thenReturnsCorrectCoordinate() {
        Map<Coordinate, Field> fields = new HashMap<>();
        Coordinate myCoord = new Coordinate(3, 4);
        fields.put(myCoord, new Field(myCoord, EGameTerrain.GRASS, null, null, EPlayerPresence.MY_PLAYER, false));
        ClientFullMap fullMap = org.mockito.Mockito.mock(ClientFullMap.class);
        org.mockito.Mockito.when(fullMap.getAllFields()).thenReturn(fields);

        PlayerPositionTracker tracker = new PlayerPositionTracker();
        Optional<Coordinate> found = tracker.findMyPosition(fullMap);
        assertTrue(found.isPresent());
        assertEquals(myCoord, found.get());
    }
}
