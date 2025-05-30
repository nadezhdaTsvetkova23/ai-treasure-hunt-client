package client.map;

import java.util.*;

public class MapUtils {
    private MapUtils() {}

    public static List<Coordinate> getNeighbors(Coordinate c) {
        List<Coordinate> neighbors = new ArrayList<>();
        neighbors.add(new Coordinate(c.getX() + 1, c.getY()));
        neighbors.add(new Coordinate(c.getX() - 1, c.getY()));
        neighbors.add(new Coordinate(c.getX(), c.getY() + 1));
        neighbors.add(new Coordinate(c.getX(), c.getY() - 1));
        return neighbors;
    }

    public static List<Coordinate> getEdgeCoordinates(int width, int height) {
        List<Coordinate> edges = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            edges.add(new Coordinate(x, 0));
            edges.add(new Coordinate(x, height - 1));
        }
        for (int y = 1; y < height - 1; y++) {
            edges.add(new Coordinate(0, y));
            edges.add(new Coordinate(width - 1, y));
        }
        return edges;
    }

    public static boolean isWalkable(Field field) {
        return field.getTerrainType() != null && field.getTerrainType().isWalkable();
    }

    public static int countWalkable(List<Field> fields) {
        int count = 0;
        for (Field f : fields) if (isWalkable(f)) count++;
        return count;
    }
}
