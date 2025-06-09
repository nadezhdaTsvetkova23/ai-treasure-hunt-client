package client.controller;

import client.gamedata.*;
import client.map.*;
import client.networking.ClientCommunicator;
import client.pathfinding.*;
import client.ui.MapVisualisator;
import client.ui.SwingMapVisualisator;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.*;

import java.util.*;

public class GameController {
    private static final long MAX_DURATION_MILLIS = 10 * 60 * 1000;
    private final ClientCommunicator communicator;
    private final DiscoveryTracker discoveryTracker;
    private final GameStateTracker gameStateTracker;
    private final PlayerPositionTracker playerPositionTracker;
    private final TechnicalInfo technicalInfo;
    private final GameInfo gameInfo;
    private final MapVisualisator mapVisualisator;
    private SwingMapVisualisator swingMapVisualisator = null;
    private final boolean guiEnabled;
    private int turnNumber = 1;

    private Coordinate treasureLocation = null;
    private Coordinate enemyFortLocation = null;

    public GameController(
            ClientCommunicator communicator,
            DiscoveryTracker discoveryTracker,
            GameStateTracker gameStateTracker,
            PlayerPositionTracker playerPositionTracker,
            TechnicalInfo technicalInfo,
            GameInfo gameInfo,
            MapVisualisator mapVisualisator,
            boolean guiEnabled
    ) {
        this.communicator = communicator;
        this.discoveryTracker = discoveryTracker;
        this.gameStateTracker = gameStateTracker;
        this.playerPositionTracker = playerPositionTracker;
        this.technicalInfo = technicalInfo;
        this.gameInfo = gameInfo;
        this.mapVisualisator = mapVisualisator;
        this.guiEnabled = guiEnabled;
    }

    public void coordinateGame() throws InterruptedException {
        UniquePlayerIdentifier playerId = communicator.registerPlayer();
        setGameInfo("Treasure Hunt", 1, "", "", "", "", "✅ Player registered: " + playerId.getUniquePlayerID());
        waitForSecondPlayer();
        HalfMap validMap = generateAndSendValidMap();
        if (validMap == null) return;

        discoveryTracker.reset();
        gameStateTracker.updateGameState(EGameState.MUST_ACT);
        playerPositionTracker.setMyPlayerPosition(null);
        playerPositionTracker.setEnemyPlayerPosition(null);

        long startTime = System.currentTimeMillis();
        runTreasureHunt(startTime);
        if (hasCollectedTreasure()) runFortHunt(startTime);
        else gameInfo.setStatus("Treasure NOT confirmed — skipping fort hunt.");
    }

    private void waitForSecondPlayer() throws InterruptedException {
        gameInfo.setStatus("Waiting for the second player to join...");
        while (true) {
            GameState state = communicator.requestFullGameState();
            if (state.getPlayers().size() < 2) {
                Thread.sleep(1000);
                continue;
            }
            Optional<PlayerState> me = state.getPlayers().stream()
                    .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                    .findFirst();
            if (me.isPresent() && EGameState.fromNetwork(me.get().getState()) == EGameState.MUST_ACT) break;
            Thread.sleep(1000);
        }
        gameInfo.setStatus("");
    }

    private HalfMap generateAndSendValidMap() {
        for (int i = 0; i < 100; i++) {
            HalfMap map = HalfMapGenerator.generateRandomMap();
            if (HalfMapValidator.validateHalfMap(map)) {
                communicator.sendHalfMap(map);
                gameInfo.setStatus("✅ Valid HalfMap generated and sent.");
                return map;
            }
        }
        technicalInfo.addError("❌ Failed to generate valid HalfMap.");
        gameInfo.setStatus("❌ Failed to generate valid HalfMap.");
        return null;
    }

    private void runTreasureHunt(long startTime) throws InterruptedException {
        gameInfo.setPhase("Treasure Hunt");
        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            ClientFullMap fullMap = communicator.receiveFullMap();

            Coordinate current = communicator.getCurrentServerPosition();
            playerPositionTracker.setMyPlayerPosition(current);
            playerPositionTracker.findEnemyPosition(fullMap)
                    .ifPresent(playerPositionTracker::setEnemyPlayerPosition);

            Map<Coordinate, Field> myFields = fullMap.getMyFields();
            Map<Coordinate, Field> allFields = fullMap.getAllFields();

            discoveryTracker.discoverField(current);

            Field currentField = allFields.get(current);
            if (currentField != null && currentField.getTerrainType() == EGameTerrain.MOUNTAIN) {
                for (Coordinate neighbor : current.getAllSurroundingCoordinates()) {
                    if (allFields.containsKey(neighbor)) {
                        discoveryTracker.discoverField(neighbor);
                    }
                }
            }
            
         // Track treasure and enemy fort location (first time it's seen)
            for (Coordinate c : discoveryTracker.getDiscoveredFields()) {
                Field f = allFields.get(c);
                if (f != null && f.isFortPresent() && !f.isMyFort() && enemyFortLocation == null) {
                    enemyFortLocation = c;
                }
                if (f != null && f.isTreasurePresent() && treasureLocation == null) {
                    treasureLocation = c;
                }
            }

            mapVisualisator.updateMap(fullMap, Set.copyOf(discoveryTracker.getDiscoveredFields()), treasureLocation);
            if (guiEnabled) {
                if (swingMapVisualisator == null)
                    swingMapVisualisator = new SwingMapVisualisator(fullMap.getWidth(), fullMap.getHeight(), gameInfo);
                swingMapVisualisator.setTreasureLocation(treasureLocation);
                swingMapVisualisator.updateMap(
                        fullMap,
                        Set.copyOf(discoveryTracker.getDiscoveredFields()),
                        playerPositionTracker.getMyPlayerPosition(),
                        playerPositionTracker.getEnemyPlayerPosition()
                );
            }

            gameInfo.setPhase("Treasure Hunt");
            gameInfo.setTurn(turnNumber);
            gameInfo.setMyPosition(String.valueOf(playerPositionTracker.getMyPlayerPosition()));
            gameInfo.setEnemyPosition(String.valueOf(playerPositionTracker.getEnemyPlayerPosition()));
            gameInfo.setTreasureFound(treasureLocation != null ? treasureLocation.toString() : "");
            gameInfo.setMove("");
            gameInfo.setStatus("");
            printGameInfoToCLI();

            turnNumber++;

            Optional<Coordinate> visibleTreasure = new TargetSearcher(myFields)
                    .getVisibleTreasure(new HashSet<>(discoveryTracker.getDiscoveredFields()));
            Coordinate target = visibleTreasure.orElseGet(() -> chooseNextExplorationTarget(current, myFields));
            if (target == null) return;

            if (!executeMoves(current, myFields, target, true)) return;
        }
        technicalInfo.addError("Timeout exceeded during treasure hunt.");
        gameInfo.setStatus("Timeout exceeded during treasure hunt.");
        printGameInfoToCLI();
    }

    private void runFortHunt(long startTime) throws InterruptedException {
        gameInfo.setPhase("Fort Hunt");
        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            ClientFullMap fullMap = communicator.receiveFullMap();

            Coordinate current = communicator.getCurrentServerPosition();
            playerPositionTracker.setMyPlayerPosition(current);
            playerPositionTracker.findEnemyPosition(fullMap)
                    .ifPresent(playerPositionTracker::setEnemyPlayerPosition);

            Map<Coordinate, Field> allFields = fullMap.getAllFields();
            Map<Coordinate, Field> enemyFields = fullMap.getEnemyFields();

            discoveryTracker.discoverField(current);

            Field currentField = allFields.get(current);
            if (currentField != null && currentField.getTerrainType() == EGameTerrain.MOUNTAIN) {
                for (Coordinate neighbor : current.getAllSurroundingCoordinates()) {
                    if (allFields.containsKey(neighbor)) {
                        discoveryTracker.discoverField(neighbor);
                    }
                }
            }
            
         // Track enemy fort location (first time it's seen)
            for (Coordinate c : discoveryTracker.getDiscoveredFields()) {
                Field f = allFields.get(c);
                if (f != null && f.isFortPresent() && !f.isMyFort() && enemyFortLocation == null) {
                    enemyFortLocation = c;
                }
            }

            mapVisualisator.updateMap(fullMap, Set.copyOf(discoveryTracker.getDiscoveredFields()), treasureLocation);
            if (guiEnabled) {
                if (swingMapVisualisator == null)
                    swingMapVisualisator = new SwingMapVisualisator(fullMap.getWidth(), fullMap.getHeight(), gameInfo);
                swingMapVisualisator.setTreasureLocation(treasureLocation);
                swingMapVisualisator.updateMap(
                        fullMap,
                        Set.copyOf(discoveryTracker.getDiscoveredFields()),
                        playerPositionTracker.getMyPlayerPosition(),
                        playerPositionTracker.getEnemyPlayerPosition()
                );
            }

            gameInfo.setPhase("Fort Hunt");
            gameInfo.setTurn(turnNumber);
            gameInfo.setMyPosition(String.valueOf(playerPositionTracker.getMyPlayerPosition()));
            gameInfo.setEnemyPosition(String.valueOf(playerPositionTracker.getEnemyPlayerPosition()));
            gameInfo.setTreasureFound(treasureLocation != null ? treasureLocation.toString() : "");
            gameInfo.setMove("");
            gameInfo.setStatus("");
            printGameInfoToCLI();

            turnNumber++;

            if (checkGameOver()) return;
            Coordinate target;
            if (enemyFortLocation != null) {
                target = enemyFortLocation;
            } else {
                target = chooseNextExplorationTarget(current, allFields);
            }

            if (target == null) {
                technicalInfo.addError("No reachable target during fort hunt.");
                gameInfo.setStatus("No reachable target during fort hunt.");
                printGameInfoToCLI();
                return;
            }

            if (!executeMoves(current, allFields, target, false)) return;
        }
        technicalInfo.addError("Timeout exceeded during fort hunt.");
        gameInfo.setStatus("Timeout exceeded during fort hunt.");
        printGameInfoToCLI();
    }

    private Coordinate chooseNextExplorationTarget(Coordinate current, Map<Coordinate, Field> fields) {
        Set<Coordinate> discovered = new HashSet<>(discoveryTracker.getDiscoveredFields());
        Coordinate bestTarget = null;
        int bestScore = Integer.MAX_VALUE;
        PathGenerator pathGen = new PathGenerator(fields);

        for (Coordinate coord : fields.keySet()) {
            if (!discovered.contains(coord) && fields.get(coord).getTerrainType().isWalkable()) {
                List<Coordinate> path = pathGen.findPathWithDijkstra(current, coord);
                if (!path.isEmpty()) {
                    int infoGain = TargetSearcher.countUnexploredNeighbors(coord, fields, discovered);
                    // Prioritize targets with higher info gain (reveals more fields)
                    int score = path.size() - (infoGain * 2); // Tweak multiplier as desired
                    if (score < bestScore) {
                        bestScore = score;
                        bestTarget = coord;
                    }
                }
            }
        }
        if (bestTarget == null) {
            gameInfo.setStatus("No more unexplored reachable targets.");
            printGameInfoToCLI();
        }
        return bestTarget;
    }
    
    private void discoverMountainNeighbors(Coordinate current, Map<Coordinate, Field> allFields) {
        Field currentField = allFields.get(current);
        if (currentField != null && currentField.getTerrainType() == EGameTerrain.MOUNTAIN) {
            for (Coordinate neighbor : current.getAllSurroundingCoordinates()) {
                if (allFields.containsKey(neighbor)) {
                    discoveryTracker.discoverField(neighbor);
                }
            }
        }
    }

    private boolean executeMoves(Coordinate current, Map<Coordinate, Field> fieldMap, Coordinate target, boolean checkTreasure) throws InterruptedException {
        PathGenerator pathGen = new PathGenerator(fieldMap);
        MoveCalculator moveCalc = new MoveCalculator(fieldMap);
        List<Coordinate> path = pathGen.findPathWithDijkstra(current, target);
        List<EClientMove> moves = moveCalc.findSequenceOfMovements(path);

        for (EClientMove move : moves) {
            EGameState state = communicator.waitUntilMustAct();
            gameStateTracker.updateGameState(state);
            if (state == EGameState.WON || state == EGameState.LOST) return false;

            communicator.sendMove(move.toServerEnum(), current, fieldMap);

            ClientFullMap updatedMap = communicator.receiveFullMap();
            current = updatedMap.findMyPlayerPosition().orElse(current);
            Map<Coordinate, Field> allFields = updatedMap.getAllFields();

            discoverMountainNeighbors(current, allFields); 

            playerPositionTracker.setMyPlayerPosition(current);
            playerPositionTracker.findEnemyPosition(updatedMap)
                    .ifPresent(playerPositionTracker::setEnemyPlayerPosition);
            discoveryTracker.discoverField(current);

            gameInfo.setMove(move.toString());
            gameInfo.setMyPosition(String.valueOf(playerPositionTracker.getMyPlayerPosition()));
            gameInfo.setEnemyPosition(String.valueOf(playerPositionTracker.getEnemyPlayerPosition()));
            gameInfo.setTreasureFound(treasureLocation != null ? treasureLocation.toString() : "");
            gameInfo.setStatus("");
            printGameInfoToCLI();

            if (guiEnabled && swingMapVisualisator != null) {
                swingMapVisualisator.setTreasureLocation(treasureLocation);
                swingMapVisualisator.updateMap(
                    communicator.receiveFullMap(),
                    Set.copyOf(discoveryTracker.getDiscoveredFields()),
                    playerPositionTracker.getMyPlayerPosition(),
                    playerPositionTracker.getEnemyPlayerPosition()
                );
            }

            if (checkTreasure) {
                if (hasCollectedTreasure()) {
                    // Mark all own fields as discovered
                    fieldMap.keySet().forEach(discoveryTracker::discoverField);
                    handleTreasureCollected(fieldMap, current);
                    gameInfo.setStatus("🎉 Treasure collected at: " + current);
                    printGameInfoToCLI();
                    return false;
                }
            }
        }
        return true;
    }


    private boolean hasCollectedTreasure() {
        GameState state = communicator.requestFullGameState();
        return state.getPlayers().stream()
                .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                .findFirst()
                .map(PlayerState::hasCollectedTreasure)
                .orElse(false);
    }

    private boolean checkGameOver() {
        GameState gameState = communicator.requestFullGameState();
        Optional<PlayerState> me = gameState.getPlayers().stream()
                .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                .findFirst();
        if (me.isPresent()) {
            EGameState state = EGameState.fromNetwork(me.get().getState());
            gameStateTracker.updateGameState(state);
            if (state == EGameState.WON) {
                gameInfo.setStatus("🎉 Fort captured. You won!");
                printGameInfoToCLI();
                return true;
            }
            if (state == EGameState.LOST) {
                gameInfo.setStatus("Game lost.");
                printGameInfoToCLI();
                return true;
            }
        }
        return false;
    }

    private void handleTreasureCollected(Map<Coordinate, Field> fieldMap, Coordinate current) {
    	treasureLocation = current; // Remember where the treasure was found
        gameInfo.setTreasureFound(current.toString());
        gameInfo.setStatus("🎉 Treasure collected at: " + current);
    }

    private void setGameInfo(String phase, int turn, String move, String myPos, String enemyPos, String treasure, String status) {
        gameInfo.setPhase(phase);
        gameInfo.setTurn(turn);
        gameInfo.setMove(move);
        gameInfo.setMyPosition(myPos);
        gameInfo.setEnemyPosition(enemyPos);
        gameInfo.setTreasureFound(treasure);
        gameInfo.setStatus(status);
        printGameInfoToCLI();
    }

    private void printGameInfoToCLI() {
        System.out.println(
            "Phase: " + gameInfo.getPhase() + "\n"
            + "Turn: " + gameInfo.getTurn() + "\n"
            + "Move: " + gameInfo.getMove() + "\n"
            + "My position: " + gameInfo.getMyPosition() + "\n"
            + "Enemy position: " + gameInfo.getEnemyPosition() + "\n"
            + "Treasure found on position: " + gameInfo.getTreasureFound() + "\n"
            + (gameInfo.getStatus() != null && !gameInfo.getStatus().isEmpty() ? ("Status: " + gameInfo.getStatus()) : "")
        );
    }

    public Coordinate getTreasureCoordinate() {
        return treasureLocation;
    }
}
