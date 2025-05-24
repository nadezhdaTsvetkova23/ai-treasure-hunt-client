package client.main;

import client.controller.GameController;
import client.networking.ClientCommunicator;
import client.ui.Visualisator;

public class MainClient {

    public static void main(String[] args) throws InterruptedException {
    	 String serverUrl = args[1];
         String gameId = args[2];

         ClientCommunicator communicator = new ClientCommunicator(
             serverUrl, gameId, "Nadezhda", "Tsvetkova", "nadezhdat97"
         );
         Visualisator visualisator = new Visualisator();

         GameController controller = new GameController(communicator, visualisator);
         controller.coordinateGame();
    }
}
