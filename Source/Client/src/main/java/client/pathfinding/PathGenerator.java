package client.pathfinding;

import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;

import java.util.*;

public class PathGenerator {
    private final Map<Coordinate, Field> fields;

    public PathGenerator(Map<Coordinate, Field> fields) {
        this.fields = fields;
    }

    public List<Coordinate> findPathWithDijkstra(Coordinate start, Coordinate target) {
        if (!isWalkable(target)) return Collections.emptyList();

        Map<Coordinate, Integer> dist = initializeDistances(start);
        Map<Coordinate, Coordinate> prev = new HashMap<>();
        PriorityQueue<Coordinate> queue = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        queue.add(start);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();
            if (current.equals(target)) break;
            processNeighbors(current, dist, prev, queue);
        }
        return buildPath(prev, start, target);
    }

    public int dijkstraDistance(Coordinate from, Coordinate to) {
        return findPathWithDijkstra(from, to).size();
    }

    public List<Coordinate> findExplorationPath(Coordinate start, List<Coordinate> targets) {
        List<Coordinate> fullPath = new ArrayList<>();
        Set<Coordinate> visited = new HashSet<>();
        Coordinate current = start;
        while (true) {
            Coordinate nextTarget = findClosestUnvisitedTarget(current, targets, visited);
            if (nextTarget == null) break;
            List<Coordinate> segment = findPathWithDijkstra(current, nextTarget);
            if (segment.size() > 1) {
                fullPath.addAll(segment.subList(1, segment.size()));
                visited.add(nextTarget);
                current = nextTarget;
            } else break;
        }
        return fullPath;
    }

    private boolean isWalkable(Coordinate coord) {
        Field f = fields.get(coord);
        return f != null && f.getTerrainType().isWalkable();
    }

    private Map<Coordinate, Integer> initializeDistances(Coordinate start) {
        Map<Coordinate, Integer> dist = new HashMap<>();
        for (Coordinate c : fields.keySet()) dist.put(c, Integer.MAX_VALUE);
        dist.put(start, 0);
        return dist;
    }

    private void processNeighbors(Coordinate current, Map<Coordinate, Integer> dist,
                                  Map<Coordinate, Coordinate> prev, PriorityQueue<Coordinate> queue) {
        Field fromField = fields.get(current);
        for (Coordinate neighbor : getWalkableNeighbors(current)) {
            int cost = calculateTransitionCost(fromField.getTerrainType(), fields.get(neighbor).getTerrainType());
            int alt = dist.get(current) + cost;
            if (alt < dist.get(neighbor)) {
                dist.put(neighbor, alt);
                prev.put(neighbor, current);
                queue.add(neighbor);
            }
        }
    }

    private List<Coordinate> buildPath(Map<Coordinate, Coordinate> prev, Coordinate start, Coordinate target) {
        List<Coordinate> path = new LinkedList<>();
        Coordinate step = target;
        while (step != null && prev.containsKey(step)) {
            path.add(0, step);
            step = prev.get(step);
        }
        if (!path.isEmpty() && !path.get(0).equals(start)) path.add(0, start);
        return path;
    }

    private List<Coordinate> getWalkableNeighbors(Coordinate c) {
        List<Coordinate> neighbors = c.getAdjacentCoordinates();
        List<Coordinate> walkable = new ArrayList<>();
        for (Coordinate n : neighbors) {
            Field f = fields.get(n);
            if (f != null && f.getTerrainType().isWalkable()) walkable.add(n);
        }
        return walkable;
    }

    private int calculateTransitionCost(EGameTerrain from, EGameTerrain to) {
        if (from == EGameTerrain.GRASS && to == EGameTerrain.GRASS) return 2;
        if ((from == EGameTerrain.GRASS && to == EGameTerrain.MOUNTAIN) ||
            (from == EGameTerrain.MOUNTAIN && to == EGameTerrain.GRASS)) return 3;
        if (from == EGameTerrain.MOUNTAIN && to == EGameTerrain.MOUNTAIN) return 4;
        return Integer.MAX_VALUE;
    }

    private Coordinate findClosestUnvisitedTarget(Coordinate current, List<Coordinate> targets, Set<Coordinate> visited) {
        Coordinate closest = null;
        int minDistance = Integer.MAX_VALUE;
        for (Coordinate t : targets) {
            if (visited.contains(t)) continue;
            int distance = dijkstraDistance(current, t);
            if (distance < minDistance) {
                minDistance = distance;
                closest = t;
            }
        }
        return closest;
    }
}
