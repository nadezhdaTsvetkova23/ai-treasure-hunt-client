package client.map;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HalfMapValidator {
    private static final Logger log = LoggerFactory.getLogger(HalfMapValidator.class);

    public static boolean validateHalfMap(HalfMap map) {
        log.info("Starting validation of HalfMap with {} fields.", map.getFields().size());
        boolean result = hasValidTerrainCounts(map)
                && hasValidFortCandidates(map)
                && hasValidEdges(map)
                && hasNoIslands(map)
                && edgesAreWalkable(map);
        log.info("HalfMap validation result: {}", result);
        return result;
    }

    static boolean hasValidTerrainCounts(HalfMap map) {
        int grass = 0, water = 0, mountain = 0;
        for (Field f : map.getFields().values()) {
            switch (f.getTerrainType()) {
                case GRASS -> grass++;
                case WATER -> water++;
                case MOUNTAIN -> mountain++;
            }
        }
        log.debug("Terrain count - Grass: {}, Water: {}, Mountain: {}", grass, water, mountain);
        return grass >= GameMapRules.MIN_GRASS_FIELDS
                && water >= GameMapRules.MIN_WATER_FIELDS
                && mountain >= GameMapRules.MIN_MOUNTAIN_FIELDS;
    }

    static boolean hasValidFortCandidates(HalfMap map) {
        long count = map.getFields().values().stream()
                .filter(Field::isFortCandidate)
                .filter(f -> f.getTerrainType() == EGameTerrain.GRASS)
                .count();
        log.debug("Fort candidate count (grass only): {}", count);
        return count == GameMapRules.FORT_CANDIDATES;
    }

    static boolean hasValidEdges(HalfMap map) {
        Set<Coordinate> coords = map.getFields().keySet();
        Set<Coordinate> valid5x10 = generateGrid(5, 10);
        Set<Coordinate> valid10x5 = generateGrid(10, 5);

        boolean is5x10 = coords.equals(valid5x10);
        boolean is10x5 = coords.equals(valid10x5);

        boolean touchesTop = coords.stream().anyMatch(c -> c.getY() == 0);
        boolean touchesBottom = coords.stream().anyMatch(c -> c.getY() == 4 || c.getY() == 9);
        boolean touchesLeft = coords.stream().anyMatch(c -> c.getX() == 0);
        boolean touchesRight = coords.stream().anyMatch(c -> c.getX() == 9 || c.getX() == 4);

        log.debug("Layout check → 5x10: {}, 10x5: {}", is5x10, is10x5);
        log.debug("Edge connectivity - Top: {}, Bottom: {}, Left: {}, Right: {}",
                touchesTop, touchesBottom, touchesLeft, touchesRight);

        return (is5x10 || is10x5) && touchesTop && touchesBottom && touchesLeft && touchesRight;
    }

    static boolean hasNoIslands(HalfMap map) {
        List<Coordinate> candidates = new ArrayList<>();
        for (Map.Entry<Coordinate, Field> entry : map.getFields().entrySet())
            if (entry.getValue().isFortCandidate()) candidates.add(entry.getKey());

        if (candidates.isEmpty())
            throw new IllegalStateException("No fort candidates for flood fill!");

        long totalNonWater = map.getFields().values().stream()
                .filter(f -> f.getTerrainType() != EGameTerrain.WATER).count();

        log.debug("Starting island check from {} fort candidate(s). Total non-water fields: {}",
                candidates.size(), totalNonWater);

        for (Coordinate start : candidates) {
            Set<Coordinate> visited = new HashSet<>();
            Deque<Coordinate> queue = new ArrayDeque<>();
            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {
                Coordinate curr = queue.poll();
                for (Coordinate n : MapUtils.getNeighbors(curr)) {
                    Field f = map.getFields().get(n);
                    if (f != null && MapUtils.isWalkable(f) && visited.add(n))
                        queue.add(n);
                }
            }

            log.debug("Flood fill from {} reached {} fields.", start, visited.size());
            if (visited.size() != totalNonWater) {
                log.warn("Flood fill from {} failed. Found disconnected region.", start);
                return false;
            }
        }
        return true;
    }

    static boolean edgesAreWalkable(HalfMap map) {
        List<Coordinate> edges = MapUtils.getEdgeCoordinates(GameMapRules.MAP_WIDTH, GameMapRules.MAP_HEIGHT);
        int walkable = 0;
        for (Coordinate c : edges) {
            Field f = map.getFields().get(c);
            if (f != null && MapUtils.isWalkable(f)) walkable++;
        }

        int required = (int) Math.ceil(edges.size() * GameMapRules.EDGE_WALKABLE_RATIO);
        log.debug("Walkable edges: {}/{} (required minimum: {})", walkable, edges.size(), required);

        return walkable >= required;
    }

    private static Set<Coordinate> generateGrid(int width, int height) {
        Set<Coordinate> result = new HashSet<>();
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                result.add(new Coordinate(x, y));
        return result;
    }
}
