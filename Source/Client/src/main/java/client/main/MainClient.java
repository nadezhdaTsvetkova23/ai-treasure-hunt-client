package client.main;

import client.controller.GameController;
import client.gamedata.*;
import client.networking.ClientCommunicator;

public class MainClient {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.err.println("Usage: java -jar Client.jar <ignored> <serverUrl> <gameId>");
            System.exit(1);
        }
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

        GameController controller = new GameController(
            communicator,
            discoveryTracker,
            gameStateTracker,
            playerPositionTracker,
            technicalInfo,
            gameInfo
        );
        controller.coordinateGame();
    }
}
