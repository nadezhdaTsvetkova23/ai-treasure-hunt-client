package client.networking;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import client.gamedata.EGameState;
import client.map.ClientFullMap;
import client.map.Coordinate;
import client.map.EGameTerrain;
import client.map.Field;
import client.map.HalfMap;
import client.map.HalfMapValidator;
import client.networking.converter.MapConverter;
import messagesbase.ResponseEnvelope;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ERequestState;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import reactor.core.publisher.Mono;

public class ClientCommunicator {
	private static final Logger log = LoggerFactory.getLogger(ClientCommunicator.class);

	private final String gameID;
	private final String url;
	private String playerID;

	private final WebClient baseWebClient;

	private final String firstName;
	private final String lastName;
	private final String uaccount;
	
	private long lastGameStateRequestTime = 0;

	public ClientCommunicator(String url, String gameID, String firstName, String lastName, String uaccount) {
	    this.url = url;
	    this.gameID = gameID;
	    this.firstName = firstName;
	    this.lastName = lastName;
	    this.uaccount = uaccount;

	    this.baseWebClient = WebClient.builder()
	            .baseUrl(url + "/games")
	            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
	            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
	            .build();
	    log.info("ClientCommunicator initialized with GameID={}", gameID);
	}

	public UniquePlayerIdentifier registerPlayer() {
        try {
            PlayerRegistration registration = new PlayerRegistration(firstName, lastName, uaccount);
            Mono<ResponseEnvelope<UniquePlayerIdentifier>> responseMono = baseWebClient
                    .method(HttpMethod.POST)
                    .uri("/" + gameID + "/players")
                    .body(BodyInserters.fromValue(registration))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {});
            ResponseEnvelope<UniquePlayerIdentifier> response = responseMono.block();

            if (response.getState() == ERequestState.Error) {
                log.error("Registration failed: {}", response.getExceptionMessage());
                throw new RuntimeException("Registration failed: " + response.getExceptionMessage());
            }

            this.playerID = response.getData().get().getUniquePlayerID();
            log.info("Player successfully registered.");
            return response.getData().get();
        } catch (Exception e) {
            log.error("Exception during player registration", e);
            throw new RuntimeException("Player registration failed.", e);
        }
    }
	
	public void sendHalfMap(HalfMap halfMap) {
        if (playerID == null) throw new IllegalStateException("Register before sending half map.");
        if (!HalfMapValidator.validateHalfMap(halfMap)) {
            log.warn("HalfMap validation failed.");
            throw new IllegalArgumentException("HalfMap invalid.");
        }

        try {
            PlayerHalfMap networkMap = MapConverter.convertToNetworkMap(halfMap, new UniquePlayerIdentifier(playerID));
            Mono<ResponseEnvelope<Void>> responseMono = baseWebClient
                    .method(HttpMethod.POST)
                    .uri("/" + gameID + "/halfmaps")
                    .body(BodyInserters.fromValue(networkMap))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<Void>>() {});
            ResponseEnvelope<Void> response = responseMono.block();

            if (response.getState() == ERequestState.Error) {
                log.error("HalfMap send failed: {}", response.getExceptionMessage());
                throw new RuntimeException("HalfMap send failed: " + response.getExceptionMessage());
            }

            log.info("HalfMap sent.");
        } catch (Exception e) {
            log.error("Exception while sending HalfMap", e);
            throw new RuntimeException("Error sending HalfMap", e);
        }
    }

	public EGameState requestGameState() {
        waitIfTooEarly();

        try {
            Mono<ResponseEnvelope<GameState>> responseMono = baseWebClient
                    .method(HttpMethod.GET)
                    .uri("/" + gameID + "/states/" + playerID)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {});
            ResponseEnvelope<GameState> response = responseMono.block();
            lastGameStateRequestTime = System.currentTimeMillis();

            if (response.getState() == ERequestState.Error) {
                log.error("Game state fetch failed: {}", response.getExceptionMessage());
                throw new RuntimeException("Game state error.");
            }

            GameState gameState = response.getData().orElseThrow(() -> new RuntimeException("Game state is empty."));
            for (PlayerState player : gameState.getPlayers()) {
                if (player.getUniquePlayerID().equals(this.playerID)) {
                    return EGameState.fromNetwork(player.getState());
                }
            }

            log.error("Player ID not found in game state.");
            throw new RuntimeException("Player ID not found.");
        } catch (Exception e) {
            log.error("Exception while requesting game state", e);
            throw new RuntimeException("Error requesting game state", e);
        }
    }
	
	public GameState requestFullGameState() {
        waitIfTooEarly();

        try {
            Mono<ResponseEnvelope<GameState>> webAccess = baseWebClient
                    .method(HttpMethod.GET)
                    .uri("/" + gameID + "/states/" + playerID)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {});
            ResponseEnvelope<GameState> response = webAccess.block();
            lastGameStateRequestTime = System.currentTimeMillis();

            if (response.getState() == ERequestState.Error) {
                log.error("Full game state error: {}", response.getExceptionMessage());
                throw new RuntimeException("Full game state error.");
            }

            return response.getData().orElseThrow(() ->
                    new RuntimeException("Missing GameState data."));
        } catch (Exception e) {
            log.error("Exception while requesting full game state", e);
            throw new RuntimeException("Error requesting full game state", e);
        }
    }
	
	public ClientFullMap receiveFullMap() {
        try {
            GameState gameState = requestFullGameState();
            ClientFullMap map = MapConverter.convertToInternalMap(
                    gameState.getMap(),
                    new UniquePlayerIdentifier(playerID)
            );
            log.info("Full map successfully received and converted.");
            return map;
        } catch (Exception e) {
            log.error("Exception while receiving full map", e);
            throw new RuntimeException("Error receiving full map", e);
        }
    }
	
	public String getPlayerID() {
	    if (playerID == null) {
	        throw new IllegalStateException("Player has not been registered yet.");
	    }
	    return playerID;
	}

	public void sendMove(EMove move, Coordinate currentPosition, Map<Coordinate, Field> fields) {
        Coordinate next = switch (move) {
            case Up -> new Coordinate(currentPosition.getX(), currentPosition.getY() - 1);
            case Down -> new Coordinate(currentPosition.getX(), currentPosition.getY() + 1);
            case Left -> new Coordinate(currentPosition.getX() - 1, currentPosition.getY());
            case Right -> new Coordinate(currentPosition.getX() + 1, currentPosition.getY());
        };

        Field target = fields.get(next);
        if (target == null || target.getTerrainType() == EGameTerrain.WATER) {
            log.warn("Invalid move attempt into {}", next);
            throw new RuntimeException("Invalid move.");
        }

        try {
            Thread.sleep(50);
            Mono<ResponseEnvelope<Void>> responseMono = baseWebClient
                    .method(HttpMethod.POST)
                    .uri("/" + gameID + "/moves")
                    .body(BodyInserters.fromValue(PlayerMove.of(playerID, move)))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<Void>>() {});
            ResponseEnvelope<Void> response = responseMono.block();

            if (response.getState() == ERequestState.Error) {
                log.error("Move failed: {}", response.getExceptionMessage());
                throw new RuntimeException("Move error.");
            }

            log.info("Move {} → {} sent.", move, next);
        } catch (Exception e) {
            log.error("Exception while sending move", e);
            throw new RuntimeException("Error sending move", e);
        }
    }
	
	public EGameState waitUntilMustAct() throws InterruptedException {
	    while (true) {
	        Thread.sleep(400);  // min wait
	        EGameState state = requestGameState();
	        if (state == EGameState.MUST_ACT) {
	        	log.info("Client may act.");
	            return state;
	        } else if (state == EGameState.WON || state == EGameState.LOST) {
	        	log.info("Game over: {}", state);
	            return state;
	        }
	    }
	}
	
	public Coordinate getCurrentServerPosition() {
		try {
	    GameState gameState = requestFullGameState();
	    return gameState.getMap().getMapNodes().stream()
	        .filter(node -> node.getPlayerPositionState().representsMyPlayer())
	        .findFirst()
	        .map(node -> new Coordinate(node.getX(), node.getY()))
	        .orElseThrow(() -> new IllegalStateException("Could not determine player's server position."));
		} catch (Exception e) {
            log.error("Exception while determining server position", e);
            throw new RuntimeException("Error locating player position", e);
        }
	}
	
	private void waitIfTooEarly() {
	    long now = System.currentTimeMillis();
	    long waitTime = 500 - (now - lastGameStateRequestTime);
	    if (waitTime > 0) {
	        try {
	            Thread.sleep(waitTime);
	            log.trace("Throttling requests, waited {}ms", waitTime);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }
	}
}
