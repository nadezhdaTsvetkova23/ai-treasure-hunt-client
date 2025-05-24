package client.networking.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.EFortPresence;
import client.map.ETreasurePresence;
import client.map.EPlayerPresence;
import client.map.Field;
import client.map.HalfMap;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
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
                    field.isFortCandidate(),
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

        // detect player starting position
        Integer myX = null;
        Integer myY = null;

        for (FullMapNode node : fullMap.getMapNodes()) {
            if (EPlayerPresence.fromServerPlayerPosition(node.getPlayerPositionState()) == EPlayerPresence.MY_PLAYER) {
                myX = node.getX();
                myY = node.getY();
                break;
            }
        }

        // Fallback: detect own fort if player not visible yet
        if (myX == null || myY == null) {
            for (FullMapNode node : fullMap.getMapNodes()) {
                if (EFortPresence.fromServerFortState(node.getFortState()) == EFortPresence.MY_FORT) {
                    myX = node.getX();
                    myY = node.getY();
                    break;
                }
            }
        }

        if (myX == null || myY == null) {
            System.out.println("Could not detect player's half map. Defaulting to left/top half.");
            myX = 0;
            myY = 0;
        }

        // detect orientation
        boolean isVerticalLayout = fullMap.getMapNodes().stream().anyMatch(n -> n.getX() > 9);
        boolean isMyHalf;

        for (FullMapNode node : fullMap.getMapNodes()) {
            Coordinate coord = new Coordinate(node.getX(), node.getY());

            EGameTerrain terrain = EGameTerrain.fromServerTerrain(node.getTerrain());
            EFortPresence fort = EFortPresence.fromServerFortState(node.getFortState());
            ETreasurePresence treasure = ETreasurePresence.fromServerTreasureState(node.getTreasureState());
            EPlayerPresence presence = EPlayerPresence.fromServerPlayerPosition(node.getPlayerPositionState());

            Field field = new Field(coord, terrain, fort, treasure, presence, false);

            if (isVerticalLayout) {
                // Horizontal layout - split by X axis
                isMyHalf = myX <= 9 ? coord.getX() <= 9 : coord.getX() >= 10;
            } else {
                // Vertical layout - split by Y axis
                isMyHalf = myY <= 4 ? coord.getY() <= 4 : coord.getY() >= 5;
            }

            if (isMyHalf) {
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
    
    public static Field updatePlayerPresence(Field oldField, EPlayerPresence newPresence) {
        return new Field(
            oldField.getCoordinate(),
            oldField.getTerrainType(),
            oldField.getFortPresence(),
            oldField.getTreasurePresence(),
            newPresence,
            false // isFortCandidate remains unchanged, we don't need it anymore after sending the initial half map
        );
    }

    private static ETerrain mapToNetworkTerrain(EGameTerrain terrain) {
        return switch (terrain) {
            case GRASS -> ETerrain.Grass;
            case MOUNTAIN -> ETerrain.Mountain;
            case WATER -> ETerrain.Water;
        };
    }
}
