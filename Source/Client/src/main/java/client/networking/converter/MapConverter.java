package client.networking.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;
import client.map.HalfMap;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;

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

    public static ClientFullMap convertToInternalMap(FullMap fullMap, UniquePlayerIdentifier myId) {
        Map<Coordinate, Field> myFields = new HashMap<>();
        Map<Coordinate, Field> enemyFields = new HashMap<>();

        int maxX = 0;
        int maxY = 0;

        for (FullMapNode node : fullMap.getMapNodes()) {
            Coordinate coord = new Coordinate(node.getX(), node.getY());
            EGameTerrain terrain = EGameTerrain.valueOf(node.getTerrain().name().toUpperCase());

            boolean isFort = node.getFortState() == EFortState.MyFortPresent || node.getFortState() == EFortState.EnemyFortPresent;
            boolean isMyFort = node.getFortState() == EFortState.MyFortPresent;

            boolean isTreasure = node.getTreasureState() == ETreasureState.MyTreasureIsPresent;

            Field field = new Field(coord, terrain, isFort, isTreasure);

            if (node.getPlayerPositionState().representsMyPlayer()) {
                myFields.put(coord, field);
            } else {
                enemyFields.put(coord, field);
            }

            maxX = Math.max(maxX, coord.getX());
            maxY = Math.max(maxY, coord.getY());
        }

        HalfMap myHalf = new HalfMap(myFields);
        HalfMap enemyHalf = new HalfMap(enemyFields);

        return new ClientFullMap(myHalf, enemyHalf, maxX + 1, maxY + 1);
    }
    
    
    private static ETerrain mapToNetworkTerrain(EGameTerrain terrain) {
        return switch (terrain) {
            case GRASS -> ETerrain.Grass;
            case MOUNTAIN -> ETerrain.Mountain;
            case WATER -> ETerrain.Water;
        };
    }
}
