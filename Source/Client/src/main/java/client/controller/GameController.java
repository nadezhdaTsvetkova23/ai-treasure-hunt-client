package client.controller;

import client.exception.InvalidCoordinateException;
import client.exception.PlayerRegistrationException;
import client.gamedata.*;
import client.map.*;
import client.networking.ClientCommunicator;
import client.pathfinding.*;
import client.ui.MapVisualisator;
import client.ui.SwingMapVisualisator;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
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
        try {
            log.info("Game coordination started.");
            UniquePlayerIdentifier playerId = communicator.registerPlayer();
            log.info("Player registered successfully: {}", playerId.getUniquePlayerID());
            setGameInfo("Treasure Hunt", 1, "", "", "", "", "Player registered: " + playerId.getUniquePlayerID());

            waitForSecondPlayer();

            HalfMap validMap = generateAndSendValidMap();
            if (validMap == null) {
                log.error("Aborting: Valid HalfMap could not be generated or sent.");
                return;
            }

            discoveryTracker.reset();
            gameStateTracker.updateGameState(EGameState.MUST_ACT);
            playerPositionTracker.setMyPlayerPosition(null);
            playerPositionTracker.setEnemyPlayerPosition(null);

            long startTime = System.currentTimeMillis();
            runTreasureHunt(startTime);
            if (hasCollectedTreasure()) {
                log.info("Treasure collected, starting Fort Hunt.");
                runFortHunt(startTime);
            } else {
                log.warn("Treasure was NOT confirmed, skipping Fort Hunt.");
                gameInfo.setStatus("Treasure NOT confirmed — skipping fort hunt.");
            }
            log.info("Game coordination finished.");
        } catch (InvalidCoordinateException e) {
            log.error("Invalid move attempted: {}", e.getMessage(), e);
            technicalInfo.addError("Invalid move: " + e.getMessage());
        } catch (PlayerRegistrationException e) {
            log.error("Player registration failed: {}", e.getMessage(), e);
            technicalInfo.addError("Player registration failed: " + e.getMessage());
            gameInfo.setStatus("Player registration failed: " + e.getMessage());
            gameInfo.printGameInfoCLI();
        } catch (Exception e) {
            log.error("Critical error during game coordination: {}", e.getMessage(), e);
            technicalInfo.addError("Critical error: " + e.getMessage());
            gameInfo.setStatus("Critical error: " + e.getMessage());
            gameInfo.printGameInfoCLI();
        }
    }

    private void waitForSecondPlayer() throws InterruptedException {
        gameInfo.setStatus("Waiting for the second player to join...");
        log.info("Waiting for second player to join...");
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
        log.info("Second player joined. Game will begin.");
    }

    private HalfMap generateAndSendValidMap() {
        log.debug("Generating and validating HalfMap...");
        for (int i = 0; i < 100; i++) {
            HalfMap map = HalfMapGenerator.generateRandomMap();
            if (HalfMapValidator.validateHalfMap(map)) {
                communicator.sendHalfMap(map);
                log.info("Valid HalfMap generated and sent.");
                gameInfo.setStatus("Valid HalfMap generated and sent.");
                return map;
            }
        }
        log.error("Failed to generate valid HalfMap after 100 attempts.");
        technicalInfo.addError("Failed to generate valid HalfMap.");
        gameInfo.setStatus("Failed to generate valid HalfMap.");
        return null;
    }

    private void runTreasureHunt(long startTime) throws InterruptedException {
        gameInfo.setPhase("Treasure Hunt");
        log.info("Phase: Treasure Hunt started.");
        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            try {
            	log.trace("Requesting new FullMap from server...");
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
                    log.debug("Mountain step at {}, neighbors revealed.", current);
                }

                // Track treasure and enemy fort location (first time it's seen)
                for (Coordinate c : discoveryTracker.getDiscoveredFields()) {
                    Field f = allFields.get(c);
                    if (f != null && f.isFortPresent() && !f.isMyFort() && enemyFortLocation == null) {
                        enemyFortLocation = c;
                        log.info("Enemy fort location discovered at {}", c);
                    }
                    if (f != null && f.isTreasurePresent() && treasureLocation == null) {
                        treasureLocation = c;
                        log.info("Treasure discovered at {}", c);
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
                gameInfo.printGameInfoCLI();

                log.debug("Turn {} in Treasure Hunt. Current position: {}", turnNumber, playerPositionTracker.getMyPlayerPosition());
                turnNumber++;

                Optional<Coordinate> visibleTreasure = new TargetSearcher(myFields)
                        .getVisibleTreasure(new HashSet<>(discoveryTracker.getDiscoveredFields()));
                Coordinate target = visibleTreasure.orElseGet(() -> chooseNextExplorationTarget(current, myFields));
                if (target == null) {
                    log.warn("No exploration target found during Treasure Hunt.");
                    return;
                }

                if (!executeMoves(current, myFields, target, true)) return;
            } catch (Exception e) {
                log.error("Error during Treasure Hunt: {}", e.getMessage(), e);
                technicalInfo.addError("Error during Treasure Hunt: " + e.getMessage());
                break;
            }
        }
        technicalInfo.addError("Timeout exceeded during treasure hunt.");
        gameInfo.setStatus("Timeout exceeded during treasure hunt.");
        log.warn("Timeout exceeded during treasure hunt.");
        gameInfo.printGameInfoCLI();
    }

    private void runFortHunt(long startTime) throws InterruptedException {
        gameInfo.setPhase("Fort Hunt");
        log.info("Phase: Fort Hunt started.");
        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            try {
            	log.trace("Requesting new FullMap from server...");
                ClientFullMap fullMap = communicator.receiveFullMap();

                Coordinate current = communicator.getCurrentServerPosition();
                playerPositionTracker.setMyPlayerPosition(current);
                playerPositionTracker.findEnemyPosition(fullMap)
                        .ifPresent(playerPositionTracker::setEnemyPlayerPosition);

                Map<Coordinate, Field> allFields = fullMap.getAllFields();

                discoveryTracker.discoverField(current);

                Field currentField = allFields.get(current);
                if (currentField != null && currentField.getTerrainType() == EGameTerrain.MOUNTAIN) {
                    for (Coordinate neighbor : current.getAllSurroundingCoordinates()) {
                        if (allFields.containsKey(neighbor)) {
                            discoveryTracker.discoverField(neighbor);
                        }
                    }
                    log.debug("Mountain step at {}, neighbors revealed.", current);
                }

                // Track enemy fort location (first time it's seen)
                for (Coordinate c : discoveryTracker.getDiscoveredFields()) {
                    Field f = allFields.get(c);
                    if (f != null && f.isFortPresent() && !f.isMyFort() && enemyFortLocation == null) {
                        enemyFortLocation = c;
                        log.info("Enemy fort location discovered at {}", c);
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
                gameInfo.printGameInfoCLI();

                log.debug("Turn {} in Fort Hunt. Current position: {}", turnNumber, playerPositionTracker.getMyPlayerPosition());
                turnNumber++;

                if (checkGameOver()) {
                    log.info("Game over detected during Fort Hunt.");
                    return;
                }
                Coordinate target;
                if (enemyFortLocation != null) {
                    target = enemyFortLocation;
                    log.info("Heading to known enemy fort at {}", enemyFortLocation);
                } else {
                    target = chooseNextExplorationTarget(current, allFields);
                }

                if (target == null) {
                    technicalInfo.addError("No reachable target during fort hunt.");
                    gameInfo.setStatus("No reachable target during fort hunt.");
                    log.warn("No reachable target during fort hunt.");
                    gameInfo.printGameInfoCLI();
                    return;
                }

                if (!executeMoves(current, allFields, target, false)) return;
            } catch (Exception e) {
                log.error("Error during Fort Hunt: {}", e.getMessage(), e);
                technicalInfo.addError("Error during Fort Hunt: " + e.getMessage());
                break;
            }
        }
        technicalInfo.addError("Timeout exceeded during fort hunt.");
        gameInfo.setStatus("Timeout exceeded during fort hunt.");
        log.warn("Timeout exceeded during fort hunt.");
        gameInfo.printGameInfoCLI();
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
                    int score = path.size() - (infoGain * 2); // Tweak multiplier as desired
                    log.trace("Evaluating exploration candidate at {}. Info gain: {}, Path length: {}", coord, infoGain, path.size());
                    if (score < bestScore) {
                        bestScore = score;
                        bestTarget = coord;
                    }
                }
            }
        }
        if (bestTarget == null) {
            gameInfo.setStatus("No more unexplored reachable targets.");
            log.warn("No more unexplored reachable targets.");
            gameInfo.printGameInfoCLI();
        } else {
            log.debug("Best exploration target selected: {}", bestTarget);
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
            log.debug("Mountain neighbors discovered from {}", current);
        }
    }

    private boolean executeMoves(Coordinate current, Map<Coordinate, Field> fieldMap, Coordinate target, boolean checkTreasure) throws InterruptedException {
        PathGenerator pathGen = new PathGenerator(fieldMap);
        MoveCalculator moveCalc = new MoveCalculator(fieldMap);
        List<Coordinate> path = pathGen.findPathWithDijkstra(current, target);
        log.trace("Calculated move path: {}", path);
        List<EClientMove> moves = moveCalc.findSequenceOfMovements(path);
        log.trace("Move sequence: {}", moves);

        for (EClientMove move : moves) {
            EGameState state = communicator.waitUntilMustAct();
            gameStateTracker.updateGameState(state);
            if (state == EGameState.WON || state == EGameState.LOST) {
                log.info("Game state: {}", state);
                return false;
            }

            log.info("Sending move {} from {} towards {}", move, current, target);
            communicator.sendMove(move.toServerEnum(), current, fieldMap);

            log.trace("Requesting new FullMap from server...");
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
            gameInfo.printGameInfoCLI();

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
                    fieldMap.keySet().forEach(discoveryTracker::discoverField);
                    handleTreasureCollected(fieldMap, current);
                    gameInfo.setStatus("🎉 Treasure collected at: " + current);
                    log.info("Treasure collected at {}", current);
                    gameInfo.printGameInfoCLI();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasCollectedTreasure() {
        try {
            GameState state = communicator.requestFullGameState();
            boolean collected = state.getPlayers().stream()
                    .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                    .findFirst()
                    .map(PlayerState::hasCollectedTreasure)
                    .orElse(false);
            if (collected) log.info("Treasure is confirmed as collected (by server).");
            return collected;
        } catch (Exception e) {
            log.error("Error checking for collected treasure: {}", e.getMessage(), e);
            return false;
        }
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
                log.info("Fort captured! Game won.");
                gameInfo.printGameInfoCLI();
                return true;
            }
            if (state == EGameState.LOST) {
                gameInfo.setStatus("Game lost.");
                log.info("Game lost.");
                gameInfo.printGameInfoCLI();
                return true;
            }
        }
        return false;
    }

    private void handleTreasureCollected(Map<Coordinate, Field> fieldMap, Coordinate current) {
    	treasureLocation = current; // Remember where the treasure was found
        gameInfo.setTreasureFound(current.toString());
        gameInfo.setStatus("🎉 Treasure collected at: " + current);
        log.info("Treasure collected and updated in game state.");
    }

    private void setGameInfo(String phase, int turn, String move, String myPos, String enemyPos, String treasure, String status) {
        gameInfo.setPhase(phase);
        gameInfo.setTurn(turn);
        gameInfo.setMove(move);
        gameInfo.setMyPosition(myPos);
        gameInfo.setEnemyPosition(enemyPos);
        gameInfo.setTreasureFound(treasure);
        gameInfo.setStatus(status);
        gameInfo.printGameInfoCLI();
    }

    public Coordinate getTreasureCoordinate() {
        return treasureLocation;
    }
}
