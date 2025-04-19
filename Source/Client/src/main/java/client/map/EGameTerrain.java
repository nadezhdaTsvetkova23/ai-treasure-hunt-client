package client.map;

import java.util.Random;

public enum EGameTerrain {
    GRASS,
    WATER,
    MOUNTAIN;

    public static EGameTerrain randomTerrain(Random random) {
        int pick = random.nextInt(3);
        return switch (pick) {
        case 0 -> GRASS;
        case 1 -> WATER;
        case 2 -> MOUNTAIN;
        default -> throw new IllegalStateException("Unexpected value: " + pick);
    };
   }
}
