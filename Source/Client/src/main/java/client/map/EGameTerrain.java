package client.map;

import java.util.Random;

import messagesbase.messagesfromclient.ETerrain;

public enum EGameTerrain {
    GRASS,
    WATER,
    MOUNTAIN;
    
    public boolean isWalkable() {
        return this != WATER;
    }
    
    public static EGameTerrain fromServerTerrain(ETerrain serverTerrain) {
        return switch (serverTerrain) {
            case Grass -> GRASS;
            case Water -> WATER;
            case Mountain -> MOUNTAIN;
        };
    }
}
