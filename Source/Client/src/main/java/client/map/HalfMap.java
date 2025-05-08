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

    public void addField(Coordinate coordinate, Field field) {
        fields.put(coordinate, field);
    }

    public Map<Coordinate, Field> getFields() {
        return fields;
    }

} 
