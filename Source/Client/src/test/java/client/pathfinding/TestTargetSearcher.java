package client.pathfinding;

import client.map.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TestTargetSearcher {

    private Map<Coordinate, Field> basicFields() {
        Map<Coordinate, Field> fields = new HashMap<>();
        fields.put(new Coordinate(0,0), new Field(new Coordinate(0,0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(0,1), new Field(new Coordinate(0,1), EGameTerrain.MOUNTAIN, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(1,0), new Field(new Coordinate(1,0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.TREASURE_PRESENT, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(1,1), new Field(new Coordinate(1,1), EGameTerrain.WATER, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        return fields;
    }

    @Test
    void givenDiscoveredTreasure_whenGetVisibleTreasure_thenReturnsTreasureCoordinate() {
        Map<Coordinate, Field> fields = basicFields();
        Set<Coordinate> discovered = Set.of(new Coordinate(1,0), new Coordinate(0,0));
        TargetSearcher searcher = new TargetSearcher(fields);
        Optional<Coordinate> treasure = searcher.getVisibleTreasure(discovered);
        
        assertTrue(treasure.isPresent());
        assertEquals(new Coordinate(1,0), treasure.get());
    }

    @Test
    void givenNoDiscoveredTreasure_whenGetVisibleTreasure_thenReturnsEmpty() {
        Map<Coordinate, Field> fields = basicFields();
        Set<Coordinate> discovered = Set.of(new Coordinate(0,0), new Coordinate(0,1));
        TargetSearcher searcher = new TargetSearcher(fields);
        
        assertTrue(searcher.getVisibleTreasure(discovered).isEmpty());
    }

    @Test
    void givenDiscoveredFields_whenSearchForExplorationTargets_thenReturnsUnexploredWalkableFields() {
        Map<Coordinate, Field> fields = basicFields();
        Set<Coordinate> discovered = Set.of(new Coordinate(0,0), new Coordinate(0,1));
        TargetSearcher searcher = new TargetSearcher(fields);
        List<Coordinate> targets = searcher.searchForExplorationTargets(discovered);
        
        assertEquals(List.of(new Coordinate(1,0)), targets);
    }

    @Test
    void givenUnexploredFields_whenSearchForUnexploredGrassFields_thenReturnsOnlyUnexploredGrassFields() {
        Map<Coordinate, Field> fields = basicFields();
        Set<Coordinate> discovered = Set.of(new Coordinate(0,0));
        TargetSearcher searcher = new TargetSearcher(fields);
        List<Coordinate> targets = searcher.searchForUnexploredGrassFields(discovered);
        
        assertTrue(targets.contains(new Coordinate(1,0)));
        assertFalse(targets.contains(new Coordinate(0,0))); 
    }

    @Test
    void givenMixedNeighbors_whenCountUnexploredNeighbors_thenReturnsCorrectCount() {
        Map<Coordinate, Field> fields = basicFields();
        Set<Coordinate> discovered = Set.of(new Coordinate(0,0));
        int count = TargetSearcher.countUnexploredNeighbors(new Coordinate(0,0), fields, discovered);
        
        // only (1,0) is grass and unexplored
        assertEquals(2, count); 
    }
}
