package client.map;

import java.util.Random;

import messagesbase.messagesfromclient.ETerrain;

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
    
    public boolean isWalkable() {
        return this != WATER;
    }

    public int getMovementCost() {
        return switch (this) {
            case GRASS -> 1;
            case MOUNTAIN -> 2;
            case WATER -> Integer.MAX_VALUE; // not walkable
        };
    }
    
    public static EGameTerrain fromServerTerrain(ETerrain serverTerrain) {
        return switch (serverTerrain) {
            case Grass -> GRASS;
            case Water -> WATER;
            case Mountain -> MOUNTAIN;
        };
    }
}
