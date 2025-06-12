package client.pathfinding;

import client.map.*;
import client.exception.InvalidCoordinateException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TestMoveCalculator {

    private Map<Coordinate, Field> simpleFields() {
        Map<Coordinate, Field> fields = new HashMap<>();
        fields.put(new Coordinate(0,0), new Field(new Coordinate(0,0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(1,0), new Field(new Coordinate(1,0), EGameTerrain.MOUNTAIN, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(2,0), new Field(new Coordinate(2,0), EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        fields.put(new Coordinate(0,1), new Field(new Coordinate(0,1), EGameTerrain.WATER, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
        return fields;
    }

    @Test
    void givenValidPath_whenFindSequenceOfMovements_thenReturnsCorrectMoves() {
        Map<Coordinate, Field> fields = simpleFields();
        MoveCalculator calculator = new MoveCalculator(fields);
        List<Coordinate> path = Arrays.asList(new Coordinate(0,0), new Coordinate(1,0), new Coordinate(2,0));
        List<EClientMove> moves = calculator.findSequenceOfMovements(path);
        
        assertEquals(6, moves.size()); // 3 for first move, 3 for second (mountain to grass)
    }

    @Test
    void givenPathWithWater_whenFindSequenceOfMovements_thenSkipsWaterMove() {
        Map<Coordinate, Field> fields = simpleFields();
        MoveCalculator calculator = new MoveCalculator(fields);
        List<Coordinate> path = Arrays.asList(new Coordinate(0,0), new Coordinate(0,1));
        List<EClientMove> moves = calculator.findSequenceOfMovements(path);
        
        assertTrue(moves.isEmpty());
    }

    @Test
    void givenNonExistentCoordinate_whenFindSequenceOfMovements_thenThrowsException() {
        Map<Coordinate, Field> fields = simpleFields();
        MoveCalculator calculator = new MoveCalculator(fields);
        List<Coordinate> path = Arrays.asList(new Coordinate(0,0), new Coordinate(5,5));
        
        assertThrows(InvalidCoordinateException.class, () -> calculator.findSequenceOfMovements(path));
    }

    @Test
    void givenSameCoordinateTwice_whenFindSequenceOfMovements_thenThrowsException() {
        Map<Coordinate, Field> fields = simpleFields();
        MoveCalculator calculator = new MoveCalculator(fields);
        List<Coordinate> path = Arrays.asList(new Coordinate(0,0), new Coordinate(0,0));
        
        assertThrows(IllegalArgumentException.class, () -> calculator.findSequenceOfMovements(path));
    }
}
