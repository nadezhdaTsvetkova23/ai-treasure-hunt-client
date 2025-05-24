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

            assignTerrains(fields, allCoordinates);

            List<Coordinate> grassCoords = getGrassCoordinates(fields);
            if (grassCoords.size() < FORT_CANDIDATES) continue;

            Set<Coordinate> fortCandidates = pickFortCandidates(grassCoords);
            markFortCandidates(fields, fortCandidates);

            placeTreasure(fields, grassCoords);

            HalfMap candidateMap = new HalfMap(fields);
            if (hasTooMuchWaterOnEdges(candidateMap)) continue;

            return candidateMap;
        }
    }

    private static void assignTerrains(Map<Coordinate, Field> fields, List<Coordinate> coordinates) {
        int mountainCount = 5;
        int waterCount = 7;
        int grassCount = TOTAL_FIELDS - mountainCount - waterCount;

        Iterator<Coordinate> iterator = coordinates.iterator();

        for (int i = 0; i < mountainCount && iterator.hasNext(); i++) {
            Coordinate c = iterator.next();
            fields.put(c, createField(c, EGameTerrain.MOUNTAIN, false));
        }

        for (int i = 0; i < waterCount && iterator.hasNext(); i++) {
            Coordinate c = iterator.next();
            fields.put(c, createField(c, EGameTerrain.WATER, false));
        }

        for (int i = 0; i < grassCount && iterator.hasNext(); i++) {
            Coordinate c = iterator.next();
            fields.put(c, createField(c, EGameTerrain.GRASS, false));
        }
    }

    private static Field createField(Coordinate c, EGameTerrain terrain, boolean isFortCandidate) {
        return new Field(c, terrain,
                EFortPresence.NO_FORT,
                ETreasurePresence.NO_TREASURE,
                EPlayerPresence.NO_PLAYER,
                isFortCandidate);
    }

    private static List<Coordinate> getGrassCoordinates(Map<Coordinate, Field> fields) {
        List<Coordinate> grassCoords = new ArrayList<>();
        for (Map.Entry<Coordinate, Field> entry : fields.entrySet()) {
            if (entry.getValue().getTerrainType() == EGameTerrain.GRASS) {
                grassCoords.add(entry.getKey());
            }
        }
        return grassCoords;
    }

    private static Set<Coordinate> pickFortCandidates(List<Coordinate> grassCoords) {
        Collections.shuffle(grassCoords, new Random());
        return new HashSet<>(grassCoords.subList(0, FORT_CANDIDATES));
    }

    private static void markFortCandidates(Map<Coordinate, Field> fields, Set<Coordinate> fortCandidates) {
        for (Coordinate c : fortCandidates) {
            Field old = fields.get(c);
            fields.put(c, new Field(c, old.getTerrainType(),
                    EFortPresence.NO_FORT,
                    ETreasurePresence.NO_TREASURE,
                    EPlayerPresence.NO_PLAYER,
                    true));
        }
    }

    private static void placeTreasure(Map<Coordinate, Field> fields, List<Coordinate> grassCoords) {
        Collections.shuffle(grassCoords, new Random());
        Coordinate treasureCoord = grassCoords.get(0);
        Field old = fields.get(treasureCoord);
        fields.put(treasureCoord, new Field(
                treasureCoord,
                old.getTerrainType(),
                old.getFortPresence(),
                ETreasurePresence.TREASURE_PRESENT,
                old.getPlayerPresence(),
                old.isFortCandidate()
        ));
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
