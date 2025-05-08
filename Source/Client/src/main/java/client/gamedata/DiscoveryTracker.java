package client.gamedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import client.map.Coordinate;

public class DiscoveryTracker {
    private final List<Coordinate> discoveredFields = new ArrayList<>();
    private boolean fortSeen = false;

    public List<Coordinate> getDiscoveredFields() {
        return discoveredFields;
    }

    public void discoverField(Coordinate coord) {
        if (!discoveredFields.contains(coord)) {
            discoveredFields.add(coord);
        }
    }
    
    public void markDiscovered(Collection<Coordinate> coords) {
        discoveredFields.addAll(coords);
    }

    public boolean isDiscovered(Coordinate coord) {
        return discoveredFields.contains(coord);
    }

    public boolean isFortSeen() {
        return fortSeen;
    }

    public void setFortSeen(boolean fortSeen) {
        this.fortSeen = fortSeen;
    }
}
