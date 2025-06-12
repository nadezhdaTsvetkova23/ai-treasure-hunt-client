package client.pathfinding;

import client.map.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TestPathGenerator {

    private Map<Coordinate, Field> smallMap() {
        Map<Coordinate, Field> fields = new HashMap<>();
        fields.put(new Coordinate(0,0), new Field(new Coordinate(0,0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(1,0), new Field(new Coordinate(1,0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(0,1), new Field(new Coordinate(0,1), EGameTerrain.MOUNTAIN, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(1,1), new Field(new Coordinate(1,1), EGameTerrain.WATER, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        return fields;
    }

    @Test
    void givenWalkablePath_whenFindPathWithDijkstra_thenReturnsShortestPath() {
        Map<Coordinate, Field> fields = smallMap();
        PathGenerator generator = new PathGenerator(fields);
        List<Coordinate> path = generator.findPathWithDijkstra(new Coordinate(0,0), new Coordinate(1,0));
        
        assertEquals(List.of(new Coordinate(0,0), new Coordinate(1,0)), path);
    }

    @Test
    void givenPathThroughMountain_whenFindPathWithDijkstra_thenCorrectCostIsReturned() {
        Map<Coordinate, Field> fields = smallMap();
        PathGenerator generator = new PathGenerator(fields);
        List<Coordinate> path = generator.findPathWithDijkstra(new Coordinate(0,0), new Coordinate(0,1));
        
        assertEquals(List.of(new Coordinate(0,0), new Coordinate(0,1)), path);
        assertEquals(2, generator.dijkstraDistance(new Coordinate(0,0), new Coordinate(0,1)));
    }

    @Test
    void givenUnwalkableTarget_whenFindPathWithDijkstra_thenReturnsEmptyList() {
        Map<Coordinate, Field> fields = smallMap();
        PathGenerator generator = new PathGenerator(fields);
        List<Coordinate> path = generator.findPathWithDijkstra(new Coordinate(0,0), new Coordinate(1,1));
        
        assertTrue(path.isEmpty());
    }

    @Test
    void givenMultipleTargets_whenFindExplorationPath_thenReturnsFullPath() {
        Map<Coordinate, Field> fields = smallMap();
        PathGenerator generator = new PathGenerator(fields);
        List<Coordinate> targets = Arrays.asList(new Coordinate(1,0), new Coordinate(0,1));
        List<Coordinate> path = generator.findExplorationPath(new Coordinate(0,0), targets);
        
        assertTrue(path.contains(new Coordinate(1,0)) || path.contains(new Coordinate(0,1)));
    }
}
