package client.map;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TestCoordinate {

    @Test
    void givenSameCoordinates_whenEqualsCalled_thenReturnsTrue() {
        Coordinate a = new Coordinate(3, 5);
        Coordinate b = new Coordinate(3, 5);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void givenDifferentCoordinates_whenEqualsCalled_thenReturnsFalse() {
        Coordinate a = new Coordinate(2, 2);
        Coordinate b = new Coordinate(2, 3);
        assertNotEquals(a, b);
    }

    @Test
    void givenCoordinate_whenGetXAndYCalled_thenReturnCorrectValues() {
        Coordinate c = new Coordinate(4, 7);
        assertEquals(4, c.getX());
        assertEquals(7, c.getY());
    }

    @Test
    void givenCenterCoordinate_whenGetAdjacentCoordinatesCalled_thenReturnsFourNeighbors() {
        Coordinate c = new Coordinate(5, 5);
        List<Coordinate> adj = c.getAdjacentCoordinates();
        assertTrue(adj.contains(new Coordinate(4, 5)));
        assertTrue(adj.contains(new Coordinate(6, 5)));
        assertTrue(adj.contains(new Coordinate(5, 4)));
        assertTrue(adj.contains(new Coordinate(5, 6)));
        assertEquals(4, adj.size());
    }

    @Test
    void givenCenterCoordinate_whenGetAllSuroundingCoordinatesCalled_thenReturnsEightNeighbors() {
        Coordinate c = new Coordinate(0, 0);
        List<Coordinate> all = c.getAllSurroundingCoordinates();
        assertEquals(8, all.size());
        assertTrue(all.contains(new Coordinate(-1, -1)));
        assertTrue(all.contains(new Coordinate(0, 1)));
        assertTrue(all.contains(new Coordinate(1, 1)));
    }
}
