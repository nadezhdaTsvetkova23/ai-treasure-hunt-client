package client.map;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestClientFullMap {

    private Field createField(Coordinate c, EPlayerPresence presence) {
        return new Field(c, EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, presence, false);
    }

    @Test
    void givenValidMap_whenGettersCalled_thenReturnCorrectValues() {
        Map<Coordinate, Field> myFields = new HashMap<>();
        Map<Coordinate, Field> enemyFields = new HashMap<>();
        myFields.put(new Coordinate(1, 1), createField(new Coordinate(1, 1), EPlayerPresence.MY_PLAYER));
        enemyFields.put(new Coordinate(2, 2), createField(new Coordinate(2, 2), EPlayerPresence.ENEMY_PLAYER));

        HalfMap myHalf = new HalfMap(myFields);
        HalfMap enemyHalf = new HalfMap(enemyFields);

        ClientFullMap map = new ClientFullMap(myHalf, enemyHalf, 3, 3);

        assertEquals(myHalf, map.getMyPlayerHalfMap());
        assertEquals(enemyHalf, map.getEnemyPlayerHalfMap());
        assertEquals(myFields, map.getMyFields());
        assertEquals(enemyFields, map.getEnemyFields());
        assertEquals(3, map.getWidth());
        assertEquals(3, map.getHeight());
    }

    @Test
    void givenMapWithTwoHalves_whenGetAllFieldsCalled_thenReturnsAllFieldsCombined() {
        Map<Coordinate, Field> myFields = new HashMap<>();
        Map<Coordinate, Field> enemyFields = new HashMap<>();
        Coordinate c1 = new Coordinate(0,0);
        Coordinate c2 = new Coordinate(2,2);
        myFields.put(c1, createField(c1, EPlayerPresence.NO_PLAYER));
        enemyFields.put(c2, createField(c2, EPlayerPresence.NO_PLAYER));

        ClientFullMap map = new ClientFullMap(new HalfMap(myFields), new HalfMap(enemyFields), 3, 3);

        Map<Coordinate, Field> combined = map.getAllFields();
        assertTrue(combined.containsKey(c1));
        assertTrue(combined.containsKey(c2));
        assertEquals(2, combined.size());
    }

    @Test
    void givenMap_whenGetFieldAtValidAndInvalidCoordinates_thenReturnsFieldOrNull() {
        Coordinate c = new Coordinate(1, 1);
        Field field = createField(c, EPlayerPresence.NO_PLAYER);
        Map<Coordinate, Field> myFields = new HashMap<>();
        myFields.put(c, field);

        ClientFullMap map = new ClientFullMap(new HalfMap(myFields), new HalfMap(new HashMap<>()), 3, 3);
        assertEquals(field, map.getFieldAt(c));
        assertNull(map.getFieldAt(new Coordinate(5, 5)));
    }

    @Test
    void givenMapWithAllOrMissingFields_whenIsCompleteCalled_thenReturnsCorrectResult() {
        Map<Coordinate, Field> myFields = new HashMap<>();
        Map<Coordinate, Field> enemyFields = new HashMap<>();
        for (int x = 0; x < 2; x++)
            for (int y = 0; y < 2; y++)
                myFields.put(new Coordinate(x, y), createField(new Coordinate(x, y), EPlayerPresence.NO_PLAYER));
        enemyFields.put(new Coordinate(2,0), createField(new Coordinate(2,0), EPlayerPresence.NO_PLAYER));
        enemyFields.put(new Coordinate(2,1), createField(new Coordinate(2,1), EPlayerPresence.NO_PLAYER));

        ClientFullMap map = new ClientFullMap(new HalfMap(myFields), new HalfMap(enemyFields), 3, 2);
        assertTrue(map.isComplete());

        ClientFullMap map2 = new ClientFullMap(new HalfMap(myFields), new HalfMap(new HashMap<>()), 3, 2);
        assertFalse(map2.isComplete());
    }

    @Test
    void givenMapWithAndWithoutPlayer_whenFindMyPlayerPositionCalled_thenReturnsExpected() {
        Coordinate myPos = new Coordinate(4, 5);
        Field myField = createField(myPos, EPlayerPresence.MY_PLAYER);
        Map<Coordinate, Field> myFields = new HashMap<>();
        myFields.put(myPos, myField);

        ClientFullMap map = new ClientFullMap(new HalfMap(myFields), new HalfMap(new HashMap<>()), 10, 10);
        Optional<Coordinate> found = map.findMyPlayerPosition();
        assertTrue(found.isPresent());
        assertEquals(myPos, found.get());

        ClientFullMap map2 = new ClientFullMap(new HalfMap(new HashMap<>()), new HalfMap(new HashMap<>()), 10, 10);
        assertTrue(map2.findMyPlayerPosition().isEmpty());
    }

    @Test
    void givenHalfMapsOfCorrectAndIncorrectSize_whenChecked_thenReturnsExpectedResult() {
        Map<Coordinate, Field> myFields = new HashMap<>();
        Map<Coordinate, Field> enemyFields = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            myFields.put(new Coordinate(i, 0), createField(new Coordinate(i, 0), EPlayerPresence.NO_PLAYER));
            enemyFields.put(new Coordinate(i, 1), createField(new Coordinate(i, 1), EPlayerPresence.NO_PLAYER));
        }
        ClientFullMap map = new ClientFullMap(new HalfMap(myFields), new HalfMap(enemyFields), 10, 10);
        assertTrue(map.isValidHalfMapSize());

        myFields.remove(new Coordinate(0,0));
        ClientFullMap map2 = new ClientFullMap(new HalfMap(myFields), new HalfMap(enemyFields), 10, 10);
        assertFalse(map2.isValidHalfMapSize());
    }
}
