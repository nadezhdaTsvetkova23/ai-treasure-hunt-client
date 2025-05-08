package client.pathfinding;

import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;

import java.util.*;

public class PathGenerator {

    private final Map<Coordinate, Field> myFields;

    public PathGenerator(Map<Coordinate, Field> myFields) {
        this.myFields = myFields;
    }

    public List<Coordinate> findPathWithDijkstra(Coordinate start, Coordinate target) {
        // prevent targeting a water tile
        Field targetField = myFields.get(target);
        if (targetField == null || targetField.getTerrainType() == EGameTerrain.WATER) {
            return Collections.emptyList(); // cannot go to water
        }

        Map<Coordinate, Integer> distance = new HashMap<>();
        Map<Coordinate, Coordinate> previous = new HashMap<>();
        PriorityQueue<Coordinate> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        for (Coordinate c : myFields.keySet()) {
            distance.put(c, Integer.MAX_VALUE);
        }

        distance.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();
            if (current.equals(target)) break;

            Field fromField = myFields.get(current);
            for (Coordinate neighbor : getNeighbors(current)) {
                if (!myFields.containsKey(neighbor)) continue;

                Field neighborField = myFields.get(neighbor);
                EGameTerrain terrain = neighborField.getTerrainType();

                if (terrain == null || terrain == EGameTerrain.WATER) {
                    continue;
                }

                int cost = calculateTransitionCost(fromField.getTerrainType(), terrain);
                int alt = distance.get(current) + cost;

                if (alt < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distance.put(neighbor, alt);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<Coordinate> path = new LinkedList<>();
        Coordinate step = target;
        while (step != null && previous.containsKey(step)) {
            path.add(0, step);
            step = previous.get(step);
        }

        if (!path.isEmpty() && !path.get(0).equals(start)) {
            path.add(0, start);
        }

        return path;
    }


    private int calculateTransitionCost(EGameTerrain from, EGameTerrain to) {
        if (from == EGameTerrain.GRASS && to == EGameTerrain.GRASS) {
            return 2;
        } else if ((from == EGameTerrain.GRASS && to == EGameTerrain.MOUNTAIN) ||
                   (from == EGameTerrain.MOUNTAIN && to == EGameTerrain.GRASS)) {
            return 3; 
        } else if (from == EGameTerrain.MOUNTAIN && to == EGameTerrain.MOUNTAIN) {
            return 4; 
        }
        return Integer.MAX_VALUE; // fallback for water or unexpected
    }


    public List<Coordinate> findExplorationPath(Coordinate start, List<Coordinate> targets) {
        List<Coordinate> fullPath = new ArrayList<>();
        Set<Coordinate> visited = new HashSet<>();
        Coordinate currentPosition = start;

        while (true) {
            Coordinate closestTarget = null;
            int minDistance = Integer.MAX_VALUE;

            for (Coordinate target : targets) {
                if (visited.contains(target)) continue;

                int distance = dijkstraDistance(currentPosition, target);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTarget = target;
                }
            }

            if (closestTarget == null) break;

            List<Coordinate> segment = findPathWithDijkstra(currentPosition, closestTarget);
            if (segment.size() > 1) {
                fullPath.addAll(segment.subList(1, segment.size())); // skip duplicate start
                visited.add(closestTarget);
                currentPosition = closestTarget;
            } else {
                break;
            }
        }

        return fullPath;
    }

    public int dijkstraDistance(Coordinate from, Coordinate to) {
        return findPathWithDijkstra(from, to).size();
    }
    
    private int calculateCost(Coordinate from, Coordinate to) {
        EGameTerrain fromTerrain = myFields.get(from).getTerrainType();
        EGameTerrain toTerrain = myFields.get(to).getTerrainType();

        int fromCost = fromTerrain == EGameTerrain.MOUNTAIN ? 4 : 2;
        int toCost = toTerrain == EGameTerrain.MOUNTAIN ? 4 : 2;

        return fromCost + toCost;
    }

    private List<Coordinate> getNeighbors(Coordinate c) {
        List<Coordinate> neighbors = List.of(
            new Coordinate(c.getX() + 1, c.getY()),
            new Coordinate(c.getX() - 1, c.getY()),
            new Coordinate(c.getX(), c.getY() + 1),
            new Coordinate(c.getX(), c.getY() - 1)
        );

        return neighbors.stream()
                .filter(myFields::containsKey)
                .filter(n -> myFields.get(n).getTerrainType().isWalkable())
                .toList();

    }
}
