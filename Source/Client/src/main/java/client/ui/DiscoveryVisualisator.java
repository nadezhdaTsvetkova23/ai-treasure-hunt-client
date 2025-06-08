package client.ui;

import client.gamedata.DiscoveryTracker;
import client.map.Coordinate;
import java.util.List;

public class DiscoveryVisualisator {
    public DiscoveryVisualisator(DiscoveryTracker model) {
        model.addPropertyChangeListener(evt -> {
            if ("discoveredFields".equals(evt.getPropertyName())) {
                @SuppressWarnings("unchecked")
                List<Coordinate> discovered = (List<Coordinate>) evt.getNewValue();
                displayDiscoveredFields(discovered);
            }
        });
    }

    private void displayDiscoveredFields(List<Coordinate> discovered) {
        System.out.print("[Discovered] Fields: ");
        for (Coordinate coord : discovered) {
            System.out.print(coord + " ");
        }
        System.out.println();
    }
}
