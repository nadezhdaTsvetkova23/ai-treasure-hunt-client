package client.map;

import java.util.HashMap;
import java.util.Map;

public class HalfMap {
    private final Map<Coordinate, Field> fields;

    public HalfMap() {
        this.fields = new HashMap<>();
    }
    
    public HalfMap(Map<Coordinate, Field> fields) {
        this.fields = fields;
    }

    public Map<Coordinate, Field> getFields() {
        return fields;
    }

} 
