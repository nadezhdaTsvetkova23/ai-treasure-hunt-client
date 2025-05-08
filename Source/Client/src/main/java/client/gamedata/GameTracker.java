package client.gamedata;

import client.map.ClientFullMap;

public class GameTracker {
    private final ClientFullMap fullMap;
    private final PlayerPositionTracker positionTracker = new PlayerPositionTracker();
    private final GameStateTracker stateTracker = new GameStateTracker();
    private final DiscoveryTracker discoveryTracker = new DiscoveryTracker();

    public GameTracker(ClientFullMap map) {
        this.fullMap = map;
    }

    public ClientFullMap getFullMap() {
        return fullMap;
    }

    public PlayerPositionTracker getPositionTracker() {
        return positionTracker;
    }

    public GameStateTracker getStateTracker() {
        return stateTracker;
    }

    public DiscoveryTracker getDiscoveryTracker() {
        return discoveryTracker;
    }
}
