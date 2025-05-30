package client.pathfinding;

import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;

import java.util.*;

public class TargetSearcher {

    private final Map<Coordinate, Field> fields;

    public TargetSearcher(Map<Coordinate, Field> fields) {
        this.fields = fields;
    }

    public Optional<Coordinate> getVisibleTreasure(Set<Coordinate> discovered) {
        for (Coordinate coord : discovered) {
            if (isTreasureVisibleAt(coord, discovered)) {
                return Optional.of(coord);
            }
        }
        return Optional.empty();
    }

    public List<Coordinate> searchForExplorationTargets(Set<Coordinate> discovered) {
        List<Coordinate> targets = new ArrayList<>();
        for (Map.Entry<Coordinate, Field> entry : fields.entrySet()) {
            if (shouldExploreField(entry.getKey(), entry.getValue(), discovered)) {
                targets.add(entry.getKey());
            }
        }
        return targets;
    }

    private boolean shouldExploreField(Coordinate coord, Field field, Set<Coordinate> discovered) {
        return field != null
                && field.getTerrainType() != null
                && field.getTerrainType().isWalkable()
                && !discovered.contains(coord);
    }

    private boolean isTreasureVisibleAt(Coordinate coord, Set<Coordinate> discovered) {
        Field field = fields.get(coord);
        if (field == null || !field.isTreasurePresent()) return false;
        return isFieldVisible(coord, discovered);
    }

    private boolean isFieldVisible(Coordinate coord, Set<Coordinate> discovered) {
        Field field = fields.get(coord);
        if (field == null) return false;
        if (field.getTerrainType() != EGameTerrain.MOUNTAIN) return true;
        return isMountainAdjacentToNonMountain(coord, discovered);
    }

    private boolean isMountainAdjacentToNonMountain(Coordinate coord, Set<Coordinate> discovered) {
        for (Coordinate neighbor : coord.getAdjacentCoordinates()) {
            Field neighborField = fields.get(neighbor);
            if (neighborField != null
                    && discovered.contains(neighbor)
                    && neighborField.getTerrainType() != EGameTerrain.MOUNTAIN) {
                return true;
            }
        }
        return false;
    }
}
