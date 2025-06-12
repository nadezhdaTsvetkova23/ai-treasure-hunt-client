package client.networking.converter;

import client.map.*;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TestMapConverter {

    @Test
    void givenValidHalfMap_whenConvertToNetworkMap_thenCorrectPlayerHalfMapCreated() {
        Coordinate coord = new Coordinate(1, 2);
        Field field = new Field(coord, EGameTerrain.GRASS, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, true);
        Map<Coordinate, Field> fields = new HashMap<>();
        fields.put(coord, field);
        HalfMap halfMap = new HalfMap(fields);
        UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("player-id");

        PlayerHalfMap networkMap = MapConverter.convertToNetworkMap(halfMap, playerId);

        assertEquals(playerId.getUniquePlayerID(), networkMap.getUniquePlayerID());
        assertEquals(1, networkMap.getMapNodes().size());
        PlayerHalfMapNode node = networkMap.getMapNodes().iterator().next();
        assertEquals(1, node.getX());
        assertEquals(2, node.getY());
        assertEquals(ETerrain.Grass, node.getTerrain());
        assertTrue(node.isFortPresent());
    }

    @Test
    void givenNullHalfMap_whenConvertToNetworkMap_thenThrowsRuntimeException() {
        UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("pid");
        assertThrows(RuntimeException.class, () -> MapConverter.convertToNetworkMap(null, playerId));
    }

    @Test
    void givenValidFullMap_whenConvertToInternalMap_thenMyAndEnemyHalvesSplitCorrectly() {
        List<FullMapNode> nodes = new ArrayList<>();
        nodes.add(new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition, ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 0, 0));
        nodes.add(new FullMapNode(ETerrain.Water, EPlayerPositionState.EnemyPlayerPosition, ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 10, 0));
        FullMap fullMap = new FullMap(nodes);
        UniquePlayerIdentifier myId = new UniquePlayerIdentifier("player-id");

        ClientFullMap clientMap = MapConverter.convertToInternalMap(fullMap, myId);

        assertTrue(clientMap.getMyPlayerHalfMap().getFields().containsKey(new Coordinate(0, 0)));
        assertTrue(clientMap.getEnemyPlayerHalfMap().getFields().containsKey(new Coordinate(10, 0)));
    }

    @Test
    void givenNullFullMap_whenConvertToInternalMap_thenThrowsRuntimeException() {
        UniquePlayerIdentifier myId = new UniquePlayerIdentifier("player-id");
        
        assertThrows(RuntimeException.class, () -> MapConverter.convertToInternalMap(null, myId));
    }

    @Test
    void givenFullMapWithoutPlayerAndFort_whenConvertToInternalMap_thenDefaultsToZeroZero() {
        List<FullMapNode> nodes = new ArrayList<>();
        
        nodes.add(new FullMapNode(ETerrain.Mountain, EPlayerPositionState.NoPlayerPresent, ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 5, 5));
        FullMap fullMap = new FullMap(nodes);
        UniquePlayerIdentifier myId = new UniquePlayerIdentifier("player-id");

        assertDoesNotThrow(() -> MapConverter.convertToInternalMap(fullMap, myId));
    }

    @Test
    void givenFieldWithAllTerrains_whenUpdatePlayerPresence_thenPresenceIsUpdatedAndTerrainIsPreserved() {
        Coordinate coord = new Coordinate(2, 3);
        for (EGameTerrain terrain : EGameTerrain.values()) {
            Field oldField = new Field(coord, terrain, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false);
            Field updated = MapConverter.updatePlayerPresence(oldField, EPlayerPresence.MY_PLAYER);
            
            assertEquals(EPlayerPresence.MY_PLAYER, updated.getPlayerPresence());
            assertEquals(coord, updated.getCoordinate());
            assertEquals(terrain, updated.getTerrainType());
            assertEquals(oldField.getFortPresence(), updated.getFortPresence());
        }
    }

    @Test
    void givenFieldWithAllTerrains_whenConvertToNetworkMap_thenTerrainIsMappedCorrectly() {
        UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("player");
        for (EGameTerrain terrain : EGameTerrain.values()) {
            Coordinate coord = new Coordinate(1, 2);
            Field field = new Field(coord, terrain, EFortPresence.NO_FORT, ETreasurePresence.NO_TREASURE, EPlayerPresence.NO_PLAYER, false);
            Map<Coordinate, Field> fields = new HashMap<>();
            fields.put(coord, field);
            HalfMap halfMap = new HalfMap(fields);

            PlayerHalfMap networkMap = MapConverter.convertToNetworkMap(halfMap, playerId);
            PlayerHalfMapNode node = networkMap.getMapNodes().iterator().next();
            switch (terrain) {
                case GRASS -> assertEquals(ETerrain.Grass, node.getTerrain());
                case MOUNTAIN -> assertEquals(ETerrain.Mountain, node.getTerrain());
                case WATER -> assertEquals(ETerrain.Water, node.getTerrain());
            }
        }
    }
}
