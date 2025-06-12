package client.gamedata;

import client.map.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class TestDiscoveryTracker {

    private DiscoveryTracker tracker;

    @BeforeEach
    void givenNewDiscoveryTracker_whenSetUp_thenTrackerIsInitialized() {
        tracker = new DiscoveryTracker();
    }

    @Test
    void givenTracker_whenDiscoverField_thenIsDiscoveredReturnsTrue_andPropertyFires() {
        AtomicBoolean propertyChanged = new AtomicBoolean(false);
        tracker.addPropertyChangeListener(evt -> {
            if ("discoveredFields".equals(evt.getPropertyName())) {
                propertyChanged.set(true);
            }
        });
        Coordinate coord = new Coordinate(3, 4);
        tracker.discoverField(coord);
        assertTrue(tracker.isDiscovered(coord));
        assertTrue(propertyChanged.get());
    }

    @Test
    void givenTracker_whenSetFortSeenTrue_thenIsFortSeenReturnsTrue_andPropertyFires() {
        AtomicBoolean propertyChanged = new AtomicBoolean(false);
        tracker.addPropertyChangeListener(evt -> {
            if ("fortSeen".equals(evt.getPropertyName())) {
                propertyChanged.set(true);
            }
        });
        tracker.setFortSeen(true);
        assertTrue(tracker.isFortSeen());
        assertTrue(propertyChanged.get());
    }

    @Test
    void givenTrackerWithFortSeen_whenSetFortSeenFalse_thenIsFortSeenReturnsFalse_andPropertyFires() {
        tracker.setFortSeen(true);
        AtomicBoolean propertyChanged = new AtomicBoolean(false);
        tracker.addPropertyChangeListener(evt -> {
            if ("fortSeen".equals(evt.getPropertyName())) {
                propertyChanged.set(true);
            }
        });
        tracker.setFortSeen(false);
        assertFalse(tracker.isFortSeen());
        assertTrue(propertyChanged.get());
    }

    @Test
    void givenTrackerWithDiscoveredFieldsAndFortSeen_whenReset_thenFieldsClearedAndFortSeenFalse_andPropertyFires() {
        tracker.discoverField(new Coordinate(1, 1));
        tracker.setFortSeen(true);

        AtomicBoolean propertyChanged = new AtomicBoolean(false);
        tracker.addPropertyChangeListener(evt -> {
            if ("discoveredFields".equals(evt.getPropertyName())) {
                propertyChanged.set(true);
            }
        });

        tracker.reset();
        assertFalse(tracker.isFortSeen());
        assertEquals(0, tracker.getDiscoveredFields().size());
        assertTrue(propertyChanged.get());
    }
}
