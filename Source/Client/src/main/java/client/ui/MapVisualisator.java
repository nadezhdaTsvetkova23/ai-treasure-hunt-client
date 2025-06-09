package client.ui;

import client.gamedata.DiscoveryTracker;
import client.gamedata.PlayerPositionTracker;
import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.EFortPresence;
import client.map.EGameTerrain;
import client.map.Field;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class MapVisualisator implements PropertyChangeListener {
    private ClientFullMap lastMap;
    private Coordinate playerPosition;
    private Coordinate enemyPosition;
    private Set<Coordinate> discovered;
    private Coordinate treasureLocation;

    public MapVisualisator(DiscoveryTracker discoveryTracker, PlayerPositionTracker positionTracker) {
        discoveryTracker.addPropertyChangeListener(this);
        positionTracker.addPropertyChangeListener(this);
        this.discovered = Set.of();
    }

    public void updateMap(ClientFullMap map, Set<Coordinate> discoveredFields, Coordinate treasureLocation) {
        this.lastMap = map;
        this.discovered = discoveredFields;
        this.treasureLocation = treasureLocation;
        displayFullMap();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "discoveredFields":
                Object value = evt.getNewValue();
                if (value instanceof List) {
                    this.discovered = Set.copyOf((List<Coordinate>) value);
                } else if (value instanceof Set) {
                    this.discovered = (Set<Coordinate>) value;
                }
                if (lastMap != null) displayFullMap();
                break;
            case "myPlayerPosition":
                this.playerPosition = (Coordinate) evt.getNewValue();
                if (lastMap != null) displayFullMap();
                break;
            case "enemyPlayerPosition":
                this.enemyPosition = (Coordinate) evt.getNewValue();
                if (lastMap != null) displayFullMap();
                break;
        }
    }

    public void displayFullMap() {
        if (lastMap == null || discovered == null) return;

        int width = lastMap.getWidth();
        int height = lastMap.getHeight();
        int cellWidth = getCellWidth(width);

        Map<Coordinate, Field> allFields = lastMap.getAllFields();

        System.out.println("\n=============== MAP UPDATE ===============");
        displayHeader(width, cellWidth);
        for (int y = 0; y < height; y++) {
            displayRowLabel(y);
            for (int x = 0; x < width; x++) {
                Coordinate coord = new Coordinate(x, y);
                Field field = allFields.get(coord);
                boolean isDiscovered = discovered.contains(coord);
                String symbol = getFieldSymbol(field, coord, playerPosition, enemyPosition, isDiscovered);
                System.out.print(padSymbol(symbol, cellWidth));
            }
            System.out.println();
        }
        displayLegend();
        printDebugFields(allFields);
        System.out.println("==========================================\n");
    }

    private int getCellWidth(int width) {
        int maxDigits = String.valueOf(width - 1).length();
        return Math.max(4, maxDigits + 2);
    }

    private void displayHeader(int width, int cellWidth) {
        System.out.print("    ");
        for (int x = 0; x < width; x++) {
            System.out.printf(" %-2s ", x);
        }
        System.out.println();
    }

    private void displayRowLabel(int y) {
        System.out.printf("%2d |", y);
    }

    private String padSymbol(String symbol, int cellWidth) {
        return String.format(" %-2s ", symbol);
    }

    private String getFieldSymbol(Field field, Coordinate coord, Coordinate player, Coordinate enemy, boolean isDiscovered) {
        if (field == null) return "??";
        if (treasureLocation != null && coord.equals(treasureLocation)) return "🟡";
        if (coord.equals(player) && coord.equals(enemy)) return "⚔️️";
        if (coord.equals(player)) return "🧑";
        if (coord.equals(enemy)) return "😈";
        if (field.isFortPresent()) {
            if (field.getFortPresence() == EFortPresence.MY_FORT) return "🏰";
            if (field.getFortPresence() == EFortPresence.ENEMY_FORT) return "🏯";
            return "🏰";
        }
        if (isDiscovered && field.isTreasurePresent()) return "🟡";

        return switch (field.getTerrainType()) {
            case GRASS -> isDiscovered ? "🟢" : "🟩";
            case MOUNTAIN -> isDiscovered ? "🟤" : "🟫️";
            case WATER -> "🟦";
        };
    }

    private void displayLegend() {
        System.out.println("\nLegend: 🟩 = Grass, 🟢 = Discovered Grass, 🟫️ = Mountain, 🟤 = Discovered Mountain, 🟦 = Water, 🏰 = My Castle, 🏯 = Enemy Castle, 🧑 = Player, 😈 = Enemy, ⚔️ = Both players, 🟡 = Treasure");
    }

    private void printDebugFields(Map<Coordinate, Field> allFields) {
        System.out.print("Discovered fields (" + discovered.size() + "): ");
        for (Coordinate coord : discovered) {
            System.out.print(coord + " ");
        }
        System.out.println();

        System.out.print("Undiscovered fields (" + (allFields.size() - discovered.size()) + "): ");
        for (Coordinate coord : allFields.keySet()) {
            if (!discovered.contains(coord)) {
                System.out.print(coord + " ");
            }
        }
        System.out.println();
    }

    public void displayCurrentMove(Coordinate from, Coordinate to, EGameTerrain terrain) {
        System.out.printf("Moved from %s to %s – Terrain: %s%n", from, to, terrain);
    }
}
