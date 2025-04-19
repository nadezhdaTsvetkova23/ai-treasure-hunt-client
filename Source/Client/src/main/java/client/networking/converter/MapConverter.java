package client.networking.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;
import client.map.HalfMap;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

public class MapConverter {

    public static PlayerHalfMap convertToNetworkMap(HalfMap internalMap, UniquePlayerIdentifier playerId) {
        List<PlayerHalfMapNode> nodes = new ArrayList<>();

        for (Map.Entry<Coordinate, Field> entry : internalMap.getFields().entrySet()) {
            Coordinate coord = entry.getKey();
            Field field = entry.getValue();

            nodes.add(new PlayerHalfMapNode(
                    coord.getX(),
                    coord.getY(),
                    field.isFortPresent(),
                    mapToNetworkTerrain(field.getTerrainType())
            ));
        }

        return new PlayerHalfMap(playerId, nodes);
    }

    private static ETerrain mapToNetworkTerrain(EGameTerrain terrain) {
        return switch (terrain) {
            case GRASS -> ETerrain.Grass;
            case MOUNTAIN -> ETerrain.Mountain;
            case WATER -> ETerrain.Water;
        };
    }
}
