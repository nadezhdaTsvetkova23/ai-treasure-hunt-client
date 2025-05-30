package client.controller;

import client.gamedata.*;
import client.map.*;
import client.networking.ClientCommunicator;
import client.pathfinding.*;
import client.ui.Visualisator;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.*;

import java.util.*;

public class GameController {
    private static final long MAX_DURATION_MILLIS = 10 * 60 * 1000;
    private final ClientCommunicator communicator;
    private final Visualisator visualisator;
    private DiscoveryTracker tracker;

    public GameController(ClientCommunicator communicator, Visualisator visualisator) {
        this.communicator = communicator;
        this.visualisator = visualisator;
    }

    public void coordinateGame() throws InterruptedException {
        UniquePlayerIdentifier playerId = communicator.registerPlayer();
        System.out.println("✅ Player registered: " + playerId.getUniquePlayerID());
        waitForSecondPlayer();
        HalfMap validMap = generateAndSendValidMap();
        if (validMap == null) return;
        tracker = new DiscoveryTracker();
        long startTime = System.currentTimeMillis();
        runTreasureHunt(startTime);
        if (hasCollectedTreasure()) runFortHunt(startTime);
        else System.out.println("Treasure NOT confirmed — skipping fort hunt.");
    }

    private void waitForSecondPlayer() throws InterruptedException {
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
    }

    private HalfMap generateAndSendValidMap() {
        for (int i = 0; i < 100; i++) {
            HalfMap map = HalfMapGenerator.generateRandomMap();
            if (HalfMapValidator.validateHalfMap(map)) {
                communicator.sendHalfMap(map);
                return map;
            }
        }
        System.out.println("❌ Failed to generate valid HalfMap.");
        return null;
    }

    private void runTreasureHunt(long startTime) throws InterruptedException {
        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            ClientFullMap fullMap = communicator.receiveFullMap();
            Coordinate current = communicator.getCurrentServerPosition();
            Map<Coordinate, Field> myFields = fullMap.getMyFields();
            displayDiscoveredAndUndiscovered(myFields);
            Optional<Coordinate> visibleTreasure = new TargetSearcher(myFields).getVisibleTreasure(new HashSet<>(tracker.getDiscoveredFields()));
            Coordinate target = visibleTreasure.orElseGet(() -> chooseNextExplorationTarget(current, myFields));
            if (target == null) return;
            visualisator.displayFullMap(fullMap, current, target);
            if (!executeMoves(current, myFields, target, true)) return;
            if (hasCollectedTreasure()) {
                System.out.println("🏆 All fields marked as discovered after treasure collection.");
                myFields.keySet().forEach(tracker::discoverField);
                return;
            }
        }
        System.out.println("Timeout exceeded during treasure hunt.");
    }

    private void runFortHunt(long startTime) throws InterruptedException {
        System.out.println("Starting enemy fort search...");
        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            ClientFullMap fullMap = communicator.receiveFullMap();
            Coordinate current = communicator.getCurrentServerPosition();
            Map<Coordinate, Field> allFields = fullMap.getAllFields();
            displayDiscoveredAndUndiscovered(allFields);
            if (checkGameOver()) return;
            Coordinate target = chooseNextExplorationTarget(current, allFields);
            if (target == null) {
                System.out.println("No reachable target.");
                return;
            }
            visualisator.displayFullMap(fullMap, current, target);
            if (!executeMoves(current, allFields, target, false)) return;
        }
        System.out.println("Timeout exceeded during fort hunt.");
    }

    private void displayDiscoveredAndUndiscovered(Map<Coordinate, Field> fields) {
        Map<Coordinate, Field> discovered = new HashMap<>(), undiscovered = new HashMap<>();
        for (Map.Entry<Coordinate, Field> entry : fields.entrySet()) {
            if (tracker.isDiscovered(entry.getKey())) discovered.put(entry.getKey(), entry.getValue());
            else undiscovered.put(entry.getKey(), entry.getValue());
        }
        visualisator.displayDiscoveredFields(discovered);
        visualisator.displayUndiscoveredFields(undiscovered);
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
            if (state == EGameState.WON) {
                System.out.println("🎉 Fort captured."); return true;
            }
            if (state == EGameState.LOST) {
                System.out.println("Game lost."); return true;
            }
        }
        return false;
    }

    private Coordinate chooseNextExplorationTarget(Coordinate current, Map<Coordinate, Field> fields) {
        Set<Coordinate> discovered = new HashSet<>(tracker.getDiscoveredFields());
        Coordinate bestTarget = null;
        int shortest = Integer.MAX_VALUE;
        PathGenerator pathGen = new PathGenerator(fields);
        for (Coordinate coord : fields.keySet()) {
            if (!discovered.contains(coord) && fields.get(coord).getTerrainType().isWalkable()) {
                List<Coordinate> path = pathGen.findPathWithDijkstra(current, coord);
                if (!path.isEmpty() && path.size() < shortest) {
                    shortest = path.size(); bestTarget = coord;
                }
            }
        }
        if (bestTarget == null) System.out.println("No more unexplored reachable targets.");
        return bestTarget;
    }

    /**
     * Executes moves and logs the treasure state after every move.
     * Also logs when the treasure is collected, including position.
     */
    private boolean executeMoves(Coordinate current, Map<Coordinate, Field> fieldMap, Coordinate target, boolean checkTreasure) throws InterruptedException {
        PathGenerator pathGen = new PathGenerator(fieldMap);
        MoveCalculator moveCalc = new MoveCalculator(fieldMap);
        List<Coordinate> path = pathGen.findPathWithDijkstra(current, target);
        List<EClientMove> moves = moveCalc.findSequenceOfMovements(path);

        for (EClientMove move : moves) {
            EGameState state = communicator.waitUntilMustAct();
            if (state == EGameState.WON || state == EGameState.LOST) return false;

            communicator.sendMove(move.toServerEnum(), current, fieldMap);
            current = communicator.receiveFullMap().findMyPlayerPosition().orElse(current);
            tracker.discoverField(current);

            if (checkTreasure) {
                logTreasureState(current);
                if (hasCollectedTreasure()) {
                    handleTreasureCollected(fieldMap, current);
                    return false;
                }
            }
        }
        return true;
    }

    private void logTreasureState(Coordinate current) {
        boolean hasTreasure = hasCollectedTreasure();
        System.out.println("Checked treasure state at " + current + ": hasCollectedTreasure = " + hasTreasure);
    }

    private void handleTreasureCollected(Map<Coordinate, Field> fieldMap, Coordinate current) {
        System.out.println("🎉 Treasure collected at: " + current);
        fieldMap.keySet().forEach(tracker::discoverField);
        System.out.println("🏆 All fields marked as discovered after treasure collection.");
    }
}
