package client.map;

import java.util.*;

public class HalfMapValidator {

    public static boolean validateHalfMap(HalfMap halfMap) {
        return checkTerrainTypeRequirements(halfMap)
                && checkCastleRequirements(halfMap)
                && checkMapEdges(halfMap)
                && !checkIfIslandsPresent(halfMap);
    }

    static boolean checkTerrainTypeRequirements(HalfMap halfMap) {
        int grass = 0, water = 0, mountain = 0;

        for (Field field : halfMap.getFields().values()) {
            switch (field.getTerrainType()) {
                case GRASS -> grass++;
                case WATER -> water++;
                case MOUNTAIN -> mountain++;
            }
        }

        return grass >= 24 && water >= 7 && mountain >= 5;
    }

    static boolean checkCastleRequirements(HalfMap halfMap) {
        long count = halfMap.getFields().values().stream()
                .filter(Field::isFortPresent)
                .filter(f -> f.getTerrainType() == EGameTerrain.GRASS)
                .count();
        return count == 1;
    }

    static boolean checkMapEdges(HalfMap halfMap) {
        Set<Coordinate> coords = halfMap.getFields().keySet();
        boolean touchesTop = coords.stream().anyMatch(c -> c.getY() == 0);
        boolean touchesBottom = coords.stream().anyMatch(c -> c.getY() == 4 || c.getY() == 9);
        boolean touchesLeft = coords.stream().anyMatch(c -> c.getX() == 0);
        boolean touchesRight = coords.stream().anyMatch(c -> c.getX() == 9 || c.getX() == 4);

        Set<Coordinate> valid5x10 = generateGrid(5, 10);
        Set<Coordinate> valid10x5 = generateGrid(10, 5);

        return (coords.equals(valid5x10) || coords.equals(valid10x5))
                && touchesTop && touchesBottom && touchesLeft && touchesRight;
    }

    static boolean checkIfIslandsPresent(HalfMap halfMap) {
        return !floodFillAlgorithm(halfMap);
    }

    static boolean floodFillAlgorithm(HalfMap halfMap) {
        Coordinate start = halfMap.getFields().entrySet().stream()
                .filter(e -> e.getValue().isFortPresent())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fort found!"));

        Set<Coordinate> visited = new HashSet<>();
        Deque<Coordinate> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();
            for (Coordinate neighbor : getNeighbors(current)) {
                Field neighborField = halfMap.getFields().get(neighbor);
                if (neighborField != null
                        && neighborField.getTerrainType() != EGameTerrain.WATER
                        && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        long totalNonWaterFields = halfMap.getFields().values().stream()
                .filter(f -> f.getTerrainType() != EGameTerrain.WATER)
                .count();

        return visited.size() == totalNonWaterFields;
    }

    private static List<Coordinate> getNeighbors(Coordinate c) {
        List<Coordinate> neighbors = new ArrayList<>();
        neighbors.add(new Coordinate(c.getX() + 1, c.getY()));
        neighbors.add(new Coordinate(c.getX() - 1, c.getY()));
        neighbors.add(new Coordinate(c.getX(), c.getY() + 1));
        neighbors.add(new Coordinate(c.getX(), c.getY() - 1));
        return neighbors;
    }

    private static Set<Coordinate> generateGrid(int width, int height) {
        Set<Coordinate> result = new HashSet<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result.add(new Coordinate(x, y));
            }
        }
        return result;
    }
}