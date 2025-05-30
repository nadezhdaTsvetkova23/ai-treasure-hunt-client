package client.map;

public class GameMapRules {
    public static final int MAP_WIDTH = 10;
    public static final int MAP_HEIGHT = 5;
    public static final int TOTAL_FIELDS = MAP_WIDTH * MAP_HEIGHT; // 50
    public static final int MIN_GRASS_FIELDS = (int) Math.ceil(TOTAL_FIELDS * 0.48); //24
    public static final int MIN_WATER_FIELDS = (int) Math.ceil(TOTAL_FIELDS * 0.14); //7
    public static final int MIN_MOUNTAIN_FIELDS = (int) Math.ceil(TOTAL_FIELDS * 0.10); //5
    public static final int FORT_CANDIDATES = (int) Math.ceil(TOTAL_FIELDS * 0.12); //6
    public static final double EDGE_WALKABLE_RATIO = 0.51; // at least 51%

    private GameMapRules() {} // Prevent instantiation
}
