package client.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
