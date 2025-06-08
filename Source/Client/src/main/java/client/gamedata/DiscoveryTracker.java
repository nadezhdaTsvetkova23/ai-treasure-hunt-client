package client.gamedata;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import client.map.Coordinate;

public class DiscoveryTracker {
    private final List<Coordinate> discoveredFields = new ArrayList<>();
    private boolean fortSeen = false;
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public List<Coordinate> getDiscoveredFields() {
        return discoveredFields;
    }

    public void discoverField(Coordinate coord) {
        if (!discoveredFields.contains(coord)) {
            discoveredFields.add(coord);
            changes.firePropertyChange("discoveredFields", null, new ArrayList<>(discoveredFields));
        }
    }
    
    public void markDiscovered(Collection<Coordinate> coords) {
        boolean updated = false;
        for (Coordinate coord : coords) {
            if (!discoveredFields.contains(coord)) {
                discoveredFields.add(coord);
                updated = true;
            }
        }
        if (updated) {
            changes.firePropertyChange("discoveredFields", null, new ArrayList<>(discoveredFields));
        }
    }

    public boolean isDiscovered(Coordinate coord) {
        return discoveredFields.contains(coord);
    }

    public boolean isFortSeen() {
        return fortSeen;
    }

    public void setFortSeen(boolean fortSeen) {
        boolean old = this.fortSeen;
        this.fortSeen = fortSeen;
        changes.firePropertyChange("fortSeen", old, fortSeen);
    }
    
    public void reset() {
        List<Coordinate> old = new ArrayList<>(discoveredFields);
        discoveredFields.clear();
        changes.firePropertyChange("discoveredFields", old, new ArrayList<>(discoveredFields));
        fortSeen = false;
    }
}
