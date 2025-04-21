package client.ui;

import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;

import java.util.Map;

public class Visualisator {

    public void displayFullMap(ClientFullMap map, Coordinate playerPosition, Coordinate enemyPosition) {
        int width = map.getWidth();
        int height = map.getHeight();
        Map<Coordinate, Field> allFields = map.getAllFields();

        System.out.println("\nMap (" + width + "x" + height + "):");

        // Header row
        System.out.print("    ");
        for (int x = 0; x < width; x++) {
            System.out.printf("%4d", x);
        }
        System.out.println();

        for (int y = 0; y < height; y++) {
            System.out.print(String.format("%2d |", y));
            for (int x = 0; x < width; x++) {
                Coordinate coord = new Coordinate(x, y);
                Field field = allFields.get(coord);
                String symbol = "??";

                if (field != null) {
                    symbol = switch (field.getTerrainType()) {
                        case GRASS -> "G";
                        case MOUNTAIN -> "M";
                        case WATER -> "W";
                    };

                    if (field.isFortPresent()) symbol = "C";
                    if (coord.equals(playerPosition)) symbol = "P";
                    else if (coord.equals(enemyPosition)) symbol = "E";
                }

                System.out.printf("   %s", symbol);
            }
            System.out.println();
        }

        System.out.println("\nLegend: G = Grass, M = Mountain, W = Water, C = Castle, P = Player, E = Enemy");
    }

    public void displayCurrentMove(Coordinate from, Coordinate to, EGameTerrain terrain) {
        System.out.printf("Moved from %s to %s – Terrain: %s%n", from, to, terrain);
    }

    public void displayGameState(String status) {
        System.out.println("Status: " + status);
    }

    public void displayDiscoveredFields(Map<Coordinate, Field> discoveredFields) {
        System.out.println("Discovered fields:");
        for (Map.Entry<Coordinate, Field> entry : discoveredFields.entrySet()) {
            Coordinate coord = entry.getKey();
            Field field = entry.getValue();
            String symbol = switch (field.getTerrainType()) {
                case GRASS -> "Grass";
                case MOUNTAIN -> "Mountain";
                case WATER -> "Water";
            };

            String extra = "";
            if (field.isFortPresent()) extra += ", Castle";
            if (field.isTreasurePresent()) extra += ", Treasure";

            System.out.printf("(%d,%d) = %s%s%n", coord.getX(), coord.getY(), symbol, extra);
        }
    }
}
