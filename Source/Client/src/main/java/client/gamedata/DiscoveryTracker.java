package client.gamedata;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.map.Coordinate;

public class DiscoveryTracker {
	private static final Logger log = LoggerFactory.getLogger(DiscoveryTracker.class);
	
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
            log.debug("Discovered new field at {}", coord);
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
        log.info("Fort was seen: {}", fortSeen);
        changes.firePropertyChange("fortSeen", old, fortSeen);
    }
    
    public void reset() {
        log.debug("Resetting DiscoveryTracker: clearing discovered fields and fortSeen flag.");
        List<Coordinate> old = new ArrayList<>(discoveredFields);
        discoveredFields.clear();
        changes.firePropertyChange("discoveredFields", old, new ArrayList<>(discoveredFields));
        fortSeen = false;
    }
}
