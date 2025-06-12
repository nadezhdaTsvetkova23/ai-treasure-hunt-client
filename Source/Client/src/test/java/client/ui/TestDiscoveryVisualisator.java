package client.ui;

import client.gamedata.DiscoveryTracker;
import client.map.Coordinate;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestDiscoveryVisualisator {

    @Test
    void givenFieldDiscovered_whenDisplayIsCalled_thenOutputContainsCoordinate() {
        
        DiscoveryTracker tracker = new DiscoveryTracker();
        DiscoveryVisualisator visualisator = new DiscoveryVisualisator(tracker);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        Coordinate coord = new Coordinate(1, 2);
        tracker.discoverField(coord);

        System.setOut(originalOut);
        assertTrue(outContent.toString().contains(coord.toString()));
    }
}
