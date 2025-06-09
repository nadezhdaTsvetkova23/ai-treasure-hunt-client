package client.main;

import client.controller.GameController;
import client.gamedata.*;
import client.networking.ClientCommunicator;
import client.ui.MapVisualisator;

public class MainClient {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.err.println("Usage: java -jar Client.jar <mode> <serverUrl> <gameId>");
            System.exit(1);
        }
        String mode = args[0];
        String serverUrl = args[1];
        String gameId = args[2];

        DiscoveryTracker discoveryTracker = new DiscoveryTracker();
        GameStateTracker gameStateTracker = new GameStateTracker();
        PlayerPositionTracker playerPositionTracker = new PlayerPositionTracker();
        TechnicalInfo technicalInfo = new TechnicalInfo();
        GameInfo gameInfo = new GameInfo();
        ClientCommunicator communicator = new ClientCommunicator(
            serverUrl, gameId, "Nadezhda", "Tsvetkova", "nadezhdat97"
        );

        MapVisualisator mapVisualisator = new MapVisualisator(discoveryTracker, playerPositionTracker);

        if ("TR".equalsIgnoreCase(mode)) {
            // CLI only
            GameController controller = new GameController(
                communicator,
                discoveryTracker,
                gameStateTracker,
                playerPositionTracker,
                technicalInfo,
                gameInfo,
                mapVisualisator,
                false 
            );
            controller.coordinateGame();
        } else {
            // GUI + CLI
            GameController controller = new GameController(
                communicator,
                discoveryTracker,
                gameStateTracker,
                playerPositionTracker,
                technicalInfo,
                gameInfo,
                mapVisualisator,
                true 
            );
            controller.coordinateGame();
        }

    }
}
