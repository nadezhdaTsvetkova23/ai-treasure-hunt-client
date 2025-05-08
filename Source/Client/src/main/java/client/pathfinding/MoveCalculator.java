package client.pathfinding;

import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;

import java.util.*;

public class MoveCalculator {

    private final Map<Coordinate, Field> fieldMap;

    public MoveCalculator(Map<Coordinate, Field> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public List<EClientMove> findSequenceOfMovements(List<Coordinate> path, Map<Coordinate, Field> fields) {
        List<EClientMove> moves = new ArrayList<>();

        for (int i = 0; i < path.size() - 1; i++) {
            Coordinate from = path.get(i);
            Coordinate to = path.get(i + 1);

            EClientMove direction = getDirection(from, to);
            EGameTerrain fromTerrain = fields.get(from).getTerrainType();
            Field toField = fields.get(to);
            if (toField == null || toField.getTerrainType() == EGameTerrain.WATER) {
                System.out.println("Skipping move to " + to + " — it's water.");
                continue;
            }
            EGameTerrain toTerrain = toField.getTerrainType();

            int repeat = 2; // default

            if (fromTerrain == EGameTerrain.MOUNTAIN && toTerrain == EGameTerrain.MOUNTAIN) {
                repeat = 4;
            } else if (fromTerrain == EGameTerrain.MOUNTAIN || toTerrain == EGameTerrain.MOUNTAIN) {
                repeat = 3;
            }

            for (int j = 0; j < repeat; j++) {
                moves.add(direction);
            }
        }
        return moves;
    }

    private EClientMove getDirection(Coordinate from, Coordinate to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();

        if (dx == 1) return EClientMove.RIGHT;
        if (dx == -1) return EClientMove.LEFT;
        if (dy == 1) return EClientMove.DOWN;
        if (dy == -1) return EClientMove.UP;

        throw new IllegalArgumentException("Invalid move from " + from + " to " + to);
    }
}
