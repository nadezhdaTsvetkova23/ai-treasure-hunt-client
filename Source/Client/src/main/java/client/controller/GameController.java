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
    private GameTracker gameTracker;
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

        GameState postTreasureState = communicator.requestFullGameState();
        Optional<PlayerState> self = postTreasureState.getPlayers().stream()
                .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                .findFirst();

        if (self.isPresent() && self.get().hasCollectedTreasure()) {
            System.out.println("Treasure has been collected — starting fort hunt...");
            runFortHunt(startTime);
        } else {
            System.out.println("Treasure NOT confirmed — skipping fort hunt.");
        }
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

            TargetSearcher searcher = new TargetSearcher(myFields);
            PathGenerator pathGen = new PathGenerator(myFields);
            MoveCalculator moveCalc = new MoveCalculator(myFields);

            Map<Coordinate, Field> discovered = new HashMap<>();
            Map<Coordinate, Field> undiscovered = new HashMap<>();
            for (Map.Entry<Coordinate, Field> entry : myFields.entrySet()) {
                if (tracker.isDiscovered(entry.getKey())) {
                    discovered.put(entry.getKey(), entry.getValue());
                } else {
                    undiscovered.put(entry.getKey(), entry.getValue());
                }
            }

            visualisator.displayDiscoveredFields(discovered);
            visualisator.displayUndiscoveredFields(undiscovered);

            Optional<Coordinate> visibleTreasure = searcher.getVisibleTreasure(new HashSet<>(tracker.getDiscoveredFields()));
            Coordinate target = visibleTreasure.orElseGet(() ->
                    chooseNextExplorationTarget(current, tracker, searcher, pathGen, myFields));
            if (target == null) return;

            visualisator.displayFullMap(fullMap, current, target);

            if (!executeMoves(current, pathGen, moveCalc, target, myFields, true, searcher)) {
                return;
            }

            GameState gameState = communicator.requestFullGameState();
            Optional<PlayerState> me = gameState.getPlayers().stream()
                    .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                    .findFirst();

            if (me.isPresent() && me.get().hasCollectedTreasure()) {
                for (Coordinate coord : myFields.keySet()) {
                    tracker.discoverField(coord);
                }
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

            TargetSearcher searcher = new TargetSearcher(allFields);
            PathGenerator pathGen = new PathGenerator(allFields);
            MoveCalculator moveCalc = new MoveCalculator(allFields);

            Map<Coordinate, Field> discovered = new HashMap<>();
            Map<Coordinate, Field> undiscovered = new HashMap<>();
            for (Map.Entry<Coordinate, Field> entry : allFields.entrySet()) {
                if (tracker.isDiscovered(entry.getKey())) {
                    discovered.put(entry.getKey(), entry.getValue());
                } else {
                    undiscovered.put(entry.getKey(), entry.getValue());
                }
            }

            visualisator.displayDiscoveredFields(discovered);
            visualisator.displayUndiscoveredFields(undiscovered);

            GameState gameState = communicator.requestFullGameState();
            Optional<PlayerState> me = gameState.getPlayers().stream()
                    .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                    .findFirst();

            if (me.isPresent()) {
                EGameState state = EGameState.fromNetwork(me.get().getState());
                if (state == EGameState.WON) {
                    System.out.println("🎉 Fort captured.");
                    return;
                } else if (state == EGameState.LOST) {
                    System.out.println("Game lost.");
                    return;
                }
            }

            Coordinate target = chooseNextExplorationTarget(current, tracker, searcher, pathGen, allFields);
            if (target == null) {
                System.out.println("No reachable target.");
                return;
            }

            visualisator.displayFullMap(fullMap, current, target);

            if (!executeMoves(current, pathGen, moveCalc, target, allFields, false, searcher)) {
                return;
            }
        }
        System.out.println("Timeout exceeded during fort hunt.");
    }

    private Coordinate chooseNextExplorationTarget(Coordinate current, DiscoveryTracker tracker,
                                                   TargetSearcher searcher, PathGenerator pathGen,
                                                   Map<Coordinate, Field> availableFields) {
        Set<Coordinate> discovered = new HashSet<>(tracker.getDiscoveredFields());
        Coordinate bestTarget = null;
        int shortest = Integer.MAX_VALUE;

        for (Coordinate coord : availableFields.keySet()) {
            if (!discovered.contains(coord) && availableFields.get(coord).getTerrainType().isWalkable()) {
                List<Coordinate> path = pathGen.findPathWithDijkstra(current, coord);
                if (!path.isEmpty() && path.size() < shortest) {
                    shortest = path.size();
                    bestTarget = coord;
                }
            }
        }

        if (bestTarget == null) System.out.println("No more unexplored reachable targets.");
        return bestTarget;
    }

    private boolean executeMoves(Coordinate current,
                                 PathGenerator pathGen,
                                 MoveCalculator moveCalc,
                                 Coordinate target,
                                 Map<Coordinate, Field> fieldMap,
                                 boolean checkTreasure,
                                 TargetSearcher searcher) throws InterruptedException {
        List<Coordinate> path = pathGen.findPathWithDijkstra(current, target);
        List<EClientMove> moves = moveCalc.findSequenceOfMovements(path, fieldMap);

        for (EClientMove move : moves) {
            EGameState state = communicator.waitUntilMustAct();
            if (state == EGameState.WON || state == EGameState.LOST) return false;

            communicator.sendMove(move.toServerEnum(), current, fieldMap);
            current = communicator.receiveFullMap().findMyPlayerPosition().orElse(current);
            tracker.discoverField(current);

            Field field = fieldMap.get(current);
            System.out.println("Checking PlayerState at " + current + ": terrain=" + field.getTerrainType());

            GameState gameState = communicator.requestFullGameState();
            Optional<PlayerState> me = gameState.getPlayers().stream()
                    .filter(p -> p.getUniquePlayerID().equals(communicator.getPlayerID()))
                    .findFirst();

            if (me.isPresent()) {
                boolean hasTreasure = me.get().hasCollectedTreasure();
                System.out.println("hasCollectedTreasure = " + hasTreasure);
                if (checkTreasure && hasTreasure) {
                    for (Coordinate coord : fieldMap.keySet()) {
                        tracker.discoverField(coord);
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
