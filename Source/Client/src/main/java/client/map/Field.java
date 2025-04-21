package client.map;

public class Field {
    private final Coordinate coordinate;
    private final EGameTerrain terrainType;
    private final boolean fortPresent;
    private final boolean treasurePresent;

    public Field(Coordinate coordinate, EGameTerrain terrainType, boolean fortPresent, boolean treasurePresent) {
        this.coordinate = coordinate;
        this.terrainType = terrainType;
        this.fortPresent = fortPresent;
        this.treasurePresent = treasurePresent;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public EGameTerrain getTerrainType() {
        return terrainType;
    }

    public boolean isFortPresent() {
        return fortPresent;
    }

    public boolean isTreasurePresent() {
        return treasurePresent;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(terrainType);
        if (fortPresent) sb.append(" [Fort]");
        if (treasurePresent) sb.append(" [Treasure]");
        return sb.toString();
    }

}
