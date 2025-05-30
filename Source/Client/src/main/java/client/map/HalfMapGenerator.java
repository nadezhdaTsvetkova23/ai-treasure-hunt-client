package client.map;

import java.util.*;

public class HalfMapGenerator {

    public static HalfMap generateRandomMap() {
        Random rand = new Random();
        while (true) {
            Map<Coordinate, Field> fields = assignTerrains(rand);
            List<Coordinate> grassCoords = getCoordsByTerrain(fields, EGameTerrain.GRASS);

            if (grassCoords.size() < GameMapRules.FORT_CANDIDATES) continue;
            Set<Coordinate> fortCandidates = pickFortCandidates(grassCoords, rand);
            markFortCandidates(fields, fortCandidates);
            placeTreasure(fields, grassCoords, rand);

            HalfMap map = new HalfMap(fields);
            if (HalfMapValidator.validateHalfMap(map)) return map;
        }
    }

    private static Map<Coordinate, Field> assignTerrains(Random rand) {
        List<Coordinate> coords = generateCoordinates();
        Collections.shuffle(coords, rand);
        Map<Coordinate, Field> fields = new HashMap<>();

        assignTerrain(fields, coords, EGameTerrain.MOUNTAIN, GameMapRules.MIN_MOUNTAIN_FIELDS, false);
        assignTerrain(fields, coords, EGameTerrain.WATER, GameMapRules.MIN_WATER_FIELDS, false);
        assignTerrain(fields, coords, EGameTerrain.GRASS, GameMapRules.TOTAL_FIELDS
                - GameMapRules.MIN_MOUNTAIN_FIELDS - GameMapRules.MIN_WATER_FIELDS, false);

        return fields;
    }

    private static void assignTerrain(Map<Coordinate, Field> fields, List<Coordinate> coords, EGameTerrain terrain, int count, boolean isFort) {
        int added = 0, i = 0;
        while (added < count && i < coords.size()) {
            Coordinate c = coords.get(i++);
            if (!fields.containsKey(c)) {
                fields.put(c, new Field(c, terrain, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, isFort));
                added++;
            }
        }
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

    private static void placeTreasure(Map<Coordinate, Field> fields, List<Coordinate> grassCoords, Random rand) {
        Collections.shuffle(grassCoords, rand);
        Coordinate treasure = grassCoords.get(0);
        Field old = fields.get(treasure);
        fields.put(treasure, new Field(treasure, old.getTerrainType(), old.getFortPresence(),
                ETreasurePresence.TREASURE_PRESENT, old.getPlayerPresence(), old.isFortCandidate()));
    }
}
