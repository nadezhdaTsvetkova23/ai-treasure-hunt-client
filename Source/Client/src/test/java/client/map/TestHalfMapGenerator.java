package client.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestHalfMapGenerator {

    @Test
    void whenGenerateRandomMapCalled_thenReturnsMapWith50Fields() {
        HalfMap map = HalfMapGenerator.generateRandomMap();
        assertEquals(50, map.getFields().size());
    }

    @Test
    void whenGenerateRandomMapCalled_thenReturnsValidMap() {
        HalfMap map = HalfMapGenerator.generateRandomMap();
        assertTrue(HalfMapValidator.validateHalfMap(map));
    }
}
