package client.networking.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger log = LoggerFactory.getLogger(MapConverter.class);

    public static PlayerHalfMap convertToNetworkMap(HalfMap internalMap, UniquePlayerIdentifier playerId) {
        List<PlayerHalfMapNode> nodes = new ArrayList<>();
        
        try {
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
        	log.debug("HalfMap successfully converted to network format with {} nodes.", nodes.size());
        	return new PlayerHalfMap(playerId, nodes);
        } catch (Exception e) {
        	log.error("Failed to convert HalfMap to network format.", e);
        	throw new RuntimeException("HalfMap conversion failed.", e);
        }
    }

    public static ClientFullMap convertToInternalMap(FullMap fullMap, UniquePlayerIdentifier myId) {
        Map<Coordinate, Field> myFields = new HashMap<>();
        Map<Coordinate, Field> enemyFields = new HashMap<>();
        int maxX = 0;
        int maxY = 0;
        Integer myX = null;
        Integer myY = null;

        try {
            for (FullMapNode node : fullMap.getMapNodes()) {
                if (EPlayerPresence.fromServerPlayerPosition(node.getPlayerPositionState()) == EPlayerPresence.MY_PLAYER) {
                    myX = node.getX();
                    myY = node.getY();
                    break;
                }
            }

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
                log.warn("Could not detect player start position. Defaulting to top-left.");
                myX = 0;
                myY = 0;
            }

            //detect orientation
            boolean isVerticalLayout = fullMap.getMapNodes().stream().anyMatch(n -> n.getX() > 9);

            for (FullMapNode node : fullMap.getMapNodes()) {
                Coordinate coord = new Coordinate(node.getX(), node.getY());
                EGameTerrain terrain = EGameTerrain.fromServerTerrain(node.getTerrain());
                EFortPresence fort = EFortPresence.fromServerFortState(node.getFortState());
                ETreasurePresence treasure = ETreasurePresence.fromServerTreasureState(node.getTreasureState());
                EPlayerPresence presence = EPlayerPresence.fromServerPlayerPosition(node.getPlayerPositionState());

                Field field = new Field(coord, terrain, fort, treasure, presence, false);

                boolean isMyHalf = isVerticalLayout
                        ? (myX <= 9 ? coord.getX() <= 9 : coord.getX() >= 10) // Horizontal layout - split by X axis
                        : (myY <= 4 ? coord.getY() <= 4 : coord.getY() >= 5); // Vertical layout - split by Y axis

                if (isMyHalf) {
                    myFields.put(coord, field);
                } else {
                    enemyFields.put(coord, field);
                }

                maxX = Math.max(maxX, coord.getX());
                maxY = Math.max(maxY, coord.getY());
            }

            log.debug("FullMap converted to internal map with dimensions {}x{}", maxX + 1, maxY + 1);
            return new ClientFullMap(new HalfMap(myFields), new HalfMap(enemyFields), maxX + 1, maxY + 1);
        } catch (Exception e) {
            log.error("Error converting FullMap to internal map.", e);
            throw new RuntimeException("FullMap conversion failed.", e);
        }
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
