package client.map;

import java.util.*;

public class HalfMapGenerator {

    private static final int WIDTH = 10;
    private static final int HEIGHT = 5;
    private static final int TOTAL_FIELDS = WIDTH * HEIGHT;

    public static HalfMap generateRandomMap() {
        Map<Coordinate, Field> fields = new HashMap<>();
        List<Coordinate> allCoordinates = generateCoordinates();

        // Shuffle coordinates for random placement
        Collections.shuffle(allCoordinates, new Random());

        // Terrain distribution
        int mountainCount = 5;
        int waterCount = 7;
        int grassCount = TOTAL_FIELDS - mountainCount - waterCount; // 38

        Iterator<Coordinate> iterator = allCoordinates.iterator();

        // Place Mountain fields
        for (int i = 0; i < mountainCount && iterator.hasNext(); i++) {
            Coordinate c = iterator.next();
            fields.put(c, new Field(c, EGameTerrain.MOUNTAIN, false, false));
        }

        // Place Water fields
        for (int i = 0; i < waterCount && iterator.hasNext(); i++) {
            Coordinate c = iterator.next();
            fields.put(c, new Field(c, EGameTerrain.WATER, false, false));
        }

        // Place Grass fields
        List<Coordinate> grassCoordinates = new ArrayList<>();
        for (int i = 0; i < grassCount && iterator.hasNext(); i++) {
            Coordinate c = iterator.next();
            fields.put(c, new Field(c, EGameTerrain.GRASS, false, false));
            grassCoordinates.add(c);
        }

        // Place fort on a random grass field
        Collections.shuffle(grassCoordinates, new Random());
        Coordinate fortCoord = grassCoordinates.get(0);
        fields.put(fortCoord, new Field(fortCoord, EGameTerrain.GRASS, true, false));

        return new HalfMap(fields);
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
