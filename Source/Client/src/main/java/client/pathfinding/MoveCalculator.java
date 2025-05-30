package client.pathfinding;

import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;

import java.util.*;

public class MoveCalculator {

    private final Map<Coordinate, Field> fields;

    public MoveCalculator(Map<Coordinate, Field> fields) {
        this.fields = fields;
    }

    public List<EClientMove> findSequenceOfMovements(List<Coordinate> path) {
        List<EClientMove> moves = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            Coordinate from = path.get(i);
            Coordinate to = path.get(i + 1);

            if (!isMoveAllowed(from, to)) continue;

            EClientMove direction = getDirection(from, to);
            int steps = calculateStepsBetween(from, to);
            addMoves(moves, direction, steps);
        }
        return moves;
    }

    private boolean isMoveAllowed(Coordinate from, Coordinate to) {
        Field toField = fields.get(to);
        if (toField == null || toField.getTerrainType() == EGameTerrain.WATER) {
        	System.out.printf("Skipping move from %s to %s: not allowed (toField=%s)%n",
                    from, to, (toField == null ? "null" : toField.getTerrainType()));
            return false;
        }
        return true;
    }

    private void addMoves(List<EClientMove> moves, EClientMove direction, int steps) {
        for (int j = 0; j < steps; j++) moves.add(direction);
    }

    private int calculateStepsBetween(Coordinate from, Coordinate to) {
        EGameTerrain fromTerrain = fields.get(from).getTerrainType();
        EGameTerrain toTerrain = fields.get(to).getTerrainType();
        if (fromTerrain == EGameTerrain.MOUNTAIN && toTerrain == EGameTerrain.MOUNTAIN) return 4;
        if (fromTerrain == EGameTerrain.MOUNTAIN || toTerrain == EGameTerrain.MOUNTAIN) return 3;
        return 2; // default for grass to grass
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
