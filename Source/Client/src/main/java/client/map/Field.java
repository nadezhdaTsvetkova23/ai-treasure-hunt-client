package client.map;

public class Field {
    private final Coordinate coordinate;
    private final EGameTerrain terrainType;
    private final EFortPresence fortPresence;
    private final ETreasurePresence treasurePresence;
    private final EPlayerPresence playerPresence;

    public Field(Coordinate coordinate, EGameTerrain terrainType,
                 EFortPresence fortPresence, ETreasurePresence treasurePresence, EPlayerPresence playerPresence) {
        this.coordinate = coordinate;
        this.terrainType = terrainType;
        this.fortPresence = fortPresence;
        this.treasurePresence = treasurePresence;
        this.playerPresence = playerPresence;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public EGameTerrain getTerrainType() {
        return terrainType;
    }

    public EFortPresence getFortPresence() {
        return fortPresence;
    }

    public ETreasurePresence getTreasurePresence() {
        return treasurePresence;
    }

    public EPlayerPresence getPlayerPresence() {
        return playerPresence;
    }

    public boolean isFortPresent() {
        return fortPresence != EFortPresence.NO_FORT;
    }

    public boolean isTreasurePresent() {
        return treasurePresence == ETreasurePresence.TREASURE_PRESENT;
    }

    public boolean isMyPlayerHere() {
        return playerPresence == EPlayerPresence.MY_PLAYER;
    }

    public boolean isEnemyPlayerHere() {
        return playerPresence == EPlayerPresence.ENEMY_PLAYER;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(terrainType);
        if (fortPresence != EFortPresence.NO_FORT) sb.append(" [Fort]");
        if (treasurePresence == ETreasurePresence.TREASURE_PRESENT) sb.append(" [Treasure]");
        if (playerPresence == EPlayerPresence.MY_PLAYER) sb.append(" [MyPlayer]");
        if (playerPresence == EPlayerPresence.ENEMY_PLAYER) sb.append(" [EnemyPlayer]");
        return sb.toString();
    }
}
