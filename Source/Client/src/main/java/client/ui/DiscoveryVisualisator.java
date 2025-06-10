package client.ui;

import client.gamedata.DiscoveryTracker;
import client.map.Coordinate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryVisualisator {
	private static final Logger log = LoggerFactory.getLogger(DiscoveryVisualisator.class);

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
        StringBuilder sb = new StringBuilder("[Discovered] Fields: ");
        for (Coordinate coord : discovered) {
            sb.append(coord).append(" ");
        }
        log.debug(sb.toString());
    }
}
