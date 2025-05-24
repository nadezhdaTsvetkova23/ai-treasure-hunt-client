package client.map;

import java.util.*;

public class HalfMapGenerator {

    private static final int WIDTH = 10;
    private static final int HEIGHT = 5;
    private static final int TOTAL_FIELDS = WIDTH * HEIGHT;
    private static final int FORT_CANDIDATES = (int) Math.round(TOTAL_FIELDS * 0.12); // 12% of 50 = 6

    public static HalfMap generateRandomMap() {
        while (true) {
            Map<Coordinate, Field> fields = new HashMap<>();
            List<Coordinate> allCoordinates = generateCoordinates();

            Collections.shuffle(allCoordinates, new Random());

            int mountainCount = 5;
            int waterCount = 7;
            int grassCount = TOTAL_FIELDS - mountainCount - waterCount;

            Iterator<Coordinate> iterator = allCoordinates.iterator();

            // place Mountains
            for (int i = 0; i < mountainCount && iterator.hasNext(); i++) {
                Coordinate c = iterator.next();
                fields.put(c, new Field(c, EGameTerrain.MOUNTAIN,
                        EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE,
                        EPlayerPresence.NO_PLAYER, false));
            }

            // place Water
            for (int i = 0; i < waterCount && iterator.hasNext(); i++) {
                Coordinate c = iterator.next();
                fields.put(c, new Field(c, EGameTerrain.WATER,
                        EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE,
                        EPlayerPresence.NO_PLAYER, false));
            }

            // place Grass
            List<Coordinate> grassCoords = new ArrayList<>();
            for (int i = 0; i < grassCount && iterator.hasNext(); i++) {
                Coordinate c = iterator.next();
                fields.put(c, new Field(c, EGameTerrain.GRASS,
                        EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE,
                        EPlayerPresence.NO_PLAYER, false));
                grassCoords.add(c);
            }

            // Select FORT_CANDIDATES grass fields for fort candidates
            if (grassCoords.size() < FORT_CANDIDATES) continue; // impossible but safe

            Collections.shuffle(grassCoords, new Random());
            Set<Coordinate> fortCandidates = new HashSet<>(grassCoords.subList(0, FORT_CANDIDATES));

            for (Coordinate c : fortCandidates) {
                Field old = fields.get(c);
                fields.put(c, new Field(c, EGameTerrain.GRASS,
                        EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE,
                        EPlayerPresence.NO_PLAYER, true));
            }

            // Treasure placement: pick a random grass field (not required by change, but required for game)
            Collections.shuffle(grassCoords, new Random());
            Coordinate treasureCoord = grassCoords.get(0);
            Field treasureField = fields.get(treasureCoord);
            fields.put(treasureCoord, new Field(treasureCoord, treasureField.getTerrainType(),
                    treasureField.getFortPresence(), ETreasurePresence.TREASURE_PRESENT,
                    treasureField.getPlayerPresence(), treasureField.isFortCandidate()));

            // You might still need to check for other validation criteria, e.g. islands, edge water
            HalfMap candidateMap = new HalfMap(fields);

            if (hasTooMuchWaterOnEdges(candidateMap)) {
                continue;
            }

            return candidateMap;
        }
    }

    private static boolean hasTooMuchWaterOnEdges(HalfMap map) {
        int top = 0, bottom = 0, left = 0, right = 0;

        for (Map.Entry<Coordinate, Field> entry : map.getFields().entrySet()) {
            Coordinate c = entry.getKey();
            Field f = entry.getValue();

            if (f.getTerrainType() != EGameTerrain.WATER) continue;

            if (c.getY() == 0) top++;
            if (c.getY() == HEIGHT - 1) bottom++;
            if (c.getX() == 0) left++;
            if (c.getX() == WIDTH - 1) right++;
        }

        return top > 2 || bottom > 2 || left > 2 || right > 2;
    }

    private static List<Coordinate> generateCoordinates() {
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                coords.add(new Coordinate(x, y));
            }
        }
        return coords;
    }
}
