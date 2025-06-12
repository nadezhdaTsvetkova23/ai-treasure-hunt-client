package client.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

class TestHalfMapValidator {

    @Test
    void givenEmptyMap_whenValidated_thenReturnsFalse() {
        HalfMap emptyMap = new HalfMap();
        assertFalse(HalfMapValidator.validateHalfMap(emptyMap));
    }

    @Test
    void givenValidMap_whenValidated_thenReturnsTrue() {
        HalfMap validMap = HalfMapGenerator.generateRandomMap();
        assertTrue(HalfMapValidator.validateHalfMap(validMap));
    }

    @Test
    void givenMapWithInvalidTerrainCounts_whenValidated_thenReturnsFalse() {
        Map<Coordinate, Field> fields = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            fields.put(new Coordinate(i % 10, i / 10),
                new Field(new Coordinate(i % 10, i / 10), EGameTerrain.WATER, EFortPresence.NO_FORT,
                    ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        }
        HalfMap map = new HalfMap(fields);

        assertFalse(HalfMapValidator.hasValidTerrainCounts(map));
        assertFalse(HalfMapValidator.validateHalfMap(map));
    }

    @Test
    void givenMapWithNotEnoughFortCandidates_whenValidated_thenReturnsFalses() {
        Map<Coordinate, Field> fields = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            fields.put(new Coordinate(i % 10, i / 10),
                new Field(new Coordinate(i % 10, i / 10), EGameTerrain.GRASS, EFortPresence.NO_FORT,
                    ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        }
        HalfMap map = new HalfMap(fields);
        assertFalse(HalfMapValidator.hasValidFortCandidates(map));
    }

    @Test
    void givenMapWithInvalidEdges_whenValidated_thenReturnsFalse() {
        Map<Coordinate, Field> fields = new HashMap<>();
        for (int x = 1; x < 9; x++)
            for (int y = 1; y < 4; y++)
                fields.put(new Coordinate(x, y),
                    new Field(new Coordinate(x, y), EGameTerrain.GRASS, EFortPresence.NO_FORT,
                        ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        HalfMap map = new HalfMap(fields);

        assertFalse(HalfMapValidator.hasValidEdges(map));
        assertFalse(HalfMapValidator.validateHalfMap(map));
    }
    
    @Test
    void givenMapWithNoFortCandidates_whenHasNoIslandsCalled_thenThrowsException() {
        Map<Coordinate, Field> fields = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            fields.put(new Coordinate(i % 10, i / 10),
                new Field(new Coordinate(i % 10, i / 10), EGameTerrain.WATER, EFortPresence.NO_FORT,
                    ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        }
        HalfMap map = new HalfMap(fields);
        assertThrows(IllegalStateException.class, () -> HalfMapValidator.hasNoIslands(map));
    }
    
    @ParameterizedTest
    @CsvSource({
        "35,8,7,true",
        "25,10,15,true",
        "24,12,14,true",
        "23,12,15,false",
        "50,0,0,false",
        "0,50,0,false"
    })
    void givenTerrainCounts_whenChecked_returnsExpectedResult(
            int grass, int water, int mountain, boolean expected) {
        Map<Coordinate, Field> fields = new HashMap<>();
        int idx = 0;
        for (int i = 0; i < grass; i++) {
            int x = idx % 10, y = idx / 10;
            fields.put(new Coordinate(x, y), new Field(new Coordinate(x, y), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
            idx++;
        }
        for (int i = 0; i < water; i++) {
            int x = idx % 10, y = idx / 10;
            fields.put(new Coordinate(x, y), new Field(new Coordinate(x, y), EGameTerrain.WATER, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
            idx++;
        }
        for (int i = 0; i < mountain; i++) {
            int x = idx % 10, y = idx / 10;
            fields.put(new Coordinate(x, y), new Field(new Coordinate(x, y), EGameTerrain.MOUNTAIN, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
            idx++;
        }
        while (fields.size() < 50) {
            int x = idx % 10, y = idx / 10;
            fields.put(new Coordinate(x, y), new Field(new Coordinate(x, y), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
            idx++;
        }
        HalfMap map = new HalfMap(fields);
        assertEquals(expected, HalfMapValidator.hasValidTerrainCounts(map));
    }
}
