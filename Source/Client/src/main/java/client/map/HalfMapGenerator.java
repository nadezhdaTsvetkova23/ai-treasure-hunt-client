package client.map;

import java.util.*;

public class HalfMapGenerator {

    private static final Random RAND = new Random();

    public static HalfMap generateRandomMap() {
        while (true) {
            Map<Coordinate, Field> fields = assignTerrainsWithSmartEdges();
            List<Coordinate> grassCoords = getCoordsByTerrain(fields, EGameTerrain.GRASS);

            List<Coordinate> goodFortCandidates = filterWellConnected(grassCoords, fields);
            if (goodFortCandidates.size() < GameMapRules.FORT_CANDIDATES) continue;
            Set<Coordinate> fortCandidates = pickFortCandidates(goodFortCandidates, RAND);

            markFortCandidates(fields, fortCandidates);

            List<Coordinate> treasureSpots = filterWellConnected(grassCoords, fields);

            HalfMap map = new HalfMap(fields);
            if (HalfMapValidator.validateHalfMap(map)) return map;
        }
    }

    private static Map<Coordinate, Field> assignTerrainsWithSmartEdges() {
        List<Coordinate> coords = generateCoordinates();
        Collections.shuffle(coords, RAND);

        Map<Coordinate, Field> fields = new HashMap<>();

        for (Coordinate c : coords) {
            if (isEdge(c)) {
                EGameTerrain edgeTerrain = (RAND.nextDouble() < 0.7) ? EGameTerrain.GRASS : EGameTerrain.MOUNTAIN;
                fields.put(c, new Field(c, edgeTerrain, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
            }
        }

        int waterToPlace = GameMapRules.MIN_WATER_FIELDS;
        List<Coordinate> shuffledNonEdge = new ArrayList<>(coords);
        Collections.shuffle(shuffledNonEdge, RAND);
        for (Coordinate c : shuffledNonEdge) {
            if (fields.containsKey(c) || waterToPlace == 0) continue;
            if (canPlaceWater(c, fields)) {
                fields.put(c, new Field(c, EGameTerrain.WATER, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
                waterToPlace--;
            }
        }

        int mountainToPlace = GameMapRules.MIN_MOUNTAIN_FIELDS;
        for (Coordinate c : coords) {
            if (!fields.containsKey(c)) {
                if (mountainToPlace > 0 && RAND.nextDouble() < 0.3) {
                    fields.put(c, new Field(c, EGameTerrain.MOUNTAIN, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
                    mountainToPlace--;
                } else {
                    fields.put(c, new Field(c, EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false));
                }
            }
        }
        return fields;
    }

    private static boolean canPlaceWater(Coordinate c, Map<Coordinate, Field> fields) {
        int consecutive = 0;
        for (Coordinate neighbor : c.getAdjacentCoordinates()) {
            Field f = fields.get(neighbor);
            if (f != null && f.getTerrainType() == EGameTerrain.WATER) consecutive++;
        }
        return consecutive < 2;
    }

    private static boolean isEdge(Coordinate c) {
        return c.getX() == 0 || c.getY() == 0 || c.getX() == GameMapRules.MAP_WIDTH - 1 || c.getY() == GameMapRules.MAP_HEIGHT - 1;
    }

    private static List<Coordinate> filterWellConnected(List<Coordinate> candidates, Map<Coordinate, Field> fields) {
        List<Coordinate> result = new ArrayList<>();
        for (Coordinate c : candidates) {
            int walkableNeighbors = 0;
            for (Coordinate n : c.getAdjacentCoordinates()) {
                Field neighbor = fields.get(n);
                if (neighbor != null && neighbor.getTerrainType().isWalkable()) walkableNeighbors++;
            }
            if (walkableNeighbors >= 2) result.add(c);
        }
        return result;
    }

    private static List<Coordinate> generateCoordinates() {
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0; x < GameMapRules.MAP_WIDTH; x++)
            for (int y = 0; y < GameMapRules.MAP_HEIGHT; y++)
                coords.add(new Coordinate(x, y));
        return coords;
    }

    private static List<Coordinate> getCoordsByTerrain(Map<Coordinate, Field> fields, EGameTerrain terrain) {
        List<Coordinate> result = new ArrayList<>();
        for (Map.Entry<Coordinate, Field> entry : fields.entrySet())
            if (entry.getValue().getTerrainType() == terrain) result.add(entry.getKey());
        return result;
    }

    private static Set<Coordinate> pickFortCandidates(List<Coordinate> grassCoords, Random rand) {
        Collections.shuffle(grassCoords, rand);
        return new HashSet<>(grassCoords.subList(0, GameMapRules.FORT_CANDIDATES));
    }

    private static void markFortCandidates(Map<Coordinate, Field> fields, Set<Coordinate> candidates) {
        for (Coordinate c : candidates) {
            Field old = fields.get(c);
            fields.put(c, new Field(c, old.getTerrainType(), EFortPresence.NO_FORT,
                    ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, true));
        }
    }

}
