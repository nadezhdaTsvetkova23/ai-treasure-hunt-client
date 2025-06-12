package client.ui;

import client.gamedata.DiscoveryTracker;
import client.gamedata.PlayerPositionTracker;
import client.map.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TestMapVisualisator {

    private MapVisualisator visualisator;
    private DiscoveryTracker discoveryTracker;
    private PlayerPositionTracker positionTracker;
    private ClientFullMap map;
    private Set<Coordinate> discovered;

    @BeforeEach
    void setUp() {
        discoveryTracker = new DiscoveryTracker();
        positionTracker = new PlayerPositionTracker();

        Map<Coordinate, Field> fields = new HashMap<>();
        fields.put(new Coordinate(0, 0), new Field(new Coordinate(0, 0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.MY_PLAYER, false));
        fields.put(new Coordinate(0, 1), new Field(new Coordinate(0, 1), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.TREASURE_PRESENT, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(1, 0), new Field(new Coordinate(1, 0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(1, 1), new Field(new Coordinate(1, 1), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.ENEMY_PLAYER, false));
        map = new ClientFullMap(new HalfMap(fields), new HalfMap(new HashMap<>()), 2, 2);

        discovered = new HashSet<>(Arrays.asList(
                new Coordinate(0, 0),
                new Coordinate(0, 1)
        ));
        visualisator = new MapVisualisator(discoveryTracker, positionTracker);
    }

    @Test
    void givenMapAndDiscoveredFields_whenUpdateMap_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> visualisator.updateMap(map, discovered, new Coordinate(0, 1)));
    }

    @Test
    void givenPropertyChangeEventForDiscoveredFields_whenPropertyChange_thenNoExceptionIsThrown() {
        PropertyChangeEvent evt = new PropertyChangeEvent(discoveryTracker, "discoveredFields", null, new ArrayList<>(discovered));
        assertDoesNotThrow(() -> visualisator.propertyChange(evt));
    }

    @Test
    void givenPropertyChangeEventForMyPlayerPosition_whenPropertyChange_thenNoExceptionIsThrown() {
        PropertyChangeEvent evt = new PropertyChangeEvent(positionTracker, "myPlayerPosition", null, new Coordinate(0, 0));
        assertDoesNotThrow(() -> visualisator.propertyChange(evt));
    }

    @Test
    void givenPropertyChangeEventForEnemyPlayerPosition_whenPropertyChange_thenNoExceptionIsThrown() {
        PropertyChangeEvent evt = new PropertyChangeEvent(positionTracker, "enemyPlayerPosition", null, new Coordinate(1, 1));
        assertDoesNotThrow(() -> visualisator.propertyChange(evt));
    }

    @Test
    void givenCoordinatesAndTerrain_whenDisplayCurrentMove_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> visualisator.displayCurrentMove(
                new Coordinate(0, 0), new Coordinate(0, 1), EGameTerrain.GRASS
        ));
    }
}
