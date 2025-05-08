package client.map;

import java.util.*;

public class HalfMapGenerator {

    private static final int WIDTH = 10;
    private static final int HEIGHT = 5;
    private static final int TOTAL_FIELDS = WIDTH * HEIGHT;

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
                fields.put(c, new Field(c, EGameTerrain.MOUNTAIN, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER));
            }

            // place Water
            for (int i = 0; i < waterCount && iterator.hasNext(); i++) {
                Coordinate c = iterator.next();
                fields.put(c, new Field(c, EGameTerrain.WATER, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER));
            }

            // place Grass
            List<Coordinate> grassCoords = new ArrayList<>();
            for (int i = 0; i < grassCount && iterator.hasNext(); i++) {
                Coordinate c = iterator.next();
                fields.put(c, new Field(c, EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER));
                grassCoords.add(c);
            }

            // Random Fort placement on grass
            Collections.shuffle(grassCoords, new Random());
            Coordinate fortCoord = grassCoords.get(0);
            fields.put(fortCoord, new Field(fortCoord, EGameTerrain.GRASS, EFortPresence.MY_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.MY_PLAYER));

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
