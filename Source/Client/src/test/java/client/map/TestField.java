package client.map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

public class TestField {
	@Test
    void givenField_whenAccessingProperties_thenCorrectValuesAreReturned() {
        Coordinate coord = new Coordinate(1,1);
        Field f = new Field(coord, EGameTerrain.GRASS, EFortPresence.MY_FORT, ETreasurePresence.TREASURE_PRESENT, EPlayerPresence.MY_PLAYER, true);

        assertTrue(f.isFortPresent());
        assertTrue(f.isTreasurePresent());
        assertTrue(f.isMyPlayerHere());
        assertFalse(f.isEnemyPlayerHere());
        assertTrue(f.isFortCandidate());
        assertTrue(f.isMyFort());
        assertFalse(f.isEnemyFort());
    }

}
