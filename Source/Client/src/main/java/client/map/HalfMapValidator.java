package client.map;

import java.util.*;

public class HalfMapValidator {

    public static boolean validateHalfMap(HalfMap halfMap) {
        return checkTerrainTypeRequirements(halfMap)
                && checkFortCandidateRequirements(halfMap)
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

    static boolean checkFortCandidateRequirements(HalfMap halfMap) {
        long count = halfMap.getFields().values().stream()
                .filter(Field::isFortCandidate)
                .filter(f -> f.getTerrainType() == EGameTerrain.GRASS)
                .count();
        return count == 6;
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

    // For each fort candidate, check if all non-water fields are reachable.
    static boolean checkIfIslandsPresent(HalfMap halfMap) {
        List<Coordinate> fortCandidates = new ArrayList<>();
        for (Map.Entry<Coordinate, Field> entry : halfMap.getFields().entrySet()) {
            if (entry.getValue().isFortCandidate()) {
                fortCandidates.add(entry.getKey());
            }
        }

        if (fortCandidates.isEmpty()) {
            throw new IllegalStateException("No fort candidates found to start flood fill!");
        }

        long totalNonWaterFields = halfMap.getFields().values().stream()
                .filter(f -> f.getTerrainType() != EGameTerrain.WATER)
                .count();

        for (Coordinate candidate : fortCandidates) {
            Set<Coordinate> visited = new HashSet<>();
            Deque<Coordinate> queue = new ArrayDeque<>();
            queue.add(candidate);
            visited.add(candidate);

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
            
            if (visited.size() != totalNonWaterFields) {
                return true; // islands exist for this candidate
            }
        }
        return false; // no islands for any candidate (the map is valid)
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
