package client.networking;

import java.util.Map;

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
	}

	public UniquePlayerIdentifier registerPlayer() {
	    PlayerRegistration registration = new PlayerRegistration(firstName, lastName, uaccount);

	    Mono<ResponseEnvelope<UniquePlayerIdentifier>> responseMono = baseWebClient
	            .method(HttpMethod.POST)
	            .uri("/" + gameID + "/players")
	            .body(BodyInserters.fromValue(registration))
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {});

	    ResponseEnvelope<UniquePlayerIdentifier> response = responseMono.block();

	    if (response.getState() == ERequestState.Error) {
	        throw new RuntimeException("!Registration failed: " + response.getExceptionMessage());
	    }

	    this.playerID = response.getData().get().getUniquePlayerID(); // store it for reuse
	    return response.getData().get();
	}
	
	public void sendHalfMap(HalfMap halfMap) {
	    if (playerID == null) {
	        throw new IllegalStateException("Player must be registered before sending half map.");
	    }

	    if (!HalfMapValidator.validateHalfMap(halfMap)) {
	        throw new IllegalArgumentException("❌ HalfMap validation failed. Map will not be sent to the server.");
	    }

	    PlayerHalfMap networkMap = MapConverter.convertToNetworkMap(halfMap, new UniquePlayerIdentifier(playerID));

	    Mono<ResponseEnvelope<Void>> responseMono = baseWebClient
	            .method(HttpMethod.POST)
	            .uri("/" + gameID + "/halfmaps")
	            .body(BodyInserters.fromValue(networkMap))
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<Void>>() {});

	    ResponseEnvelope<Void> response = responseMono.block();

	    if (response.getState() == ERequestState.Error) {
	        throw new RuntimeException("❌ HalfMap sending failed: " + response.getExceptionMessage());
	    }

	    System.out.println("✅ HalfMap sent successfully!");
	}

	public EGameState requestGameState() {
	    waitIfTooEarly();

	    Mono<ResponseEnvelope<GameState>> responseMono = baseWebClient
	            .method(HttpMethod.GET)
	            .uri("/" + gameID + "/states/" + playerID)
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {});

	    ResponseEnvelope<GameState> response = responseMono.block();
	    lastGameStateRequestTime = System.currentTimeMillis();

	    if (response.getState() == ERequestState.Error) {
	        throw new RuntimeException("Failed to get game state: " + response.getExceptionMessage());
	    }

	    GameState gameState = response.getData().orElseThrow(() ->
	        new RuntimeException("No GameState returned.")
	    );

	    for (PlayerState player : gameState.getPlayers()) {
	        if (player.getUniquePlayerID().equals(this.playerID)) {
	            return EGameState.fromNetwork(player.getState());
	        }
	    }
	    throw new RuntimeException("Could not find own player in game state.");
	}

	
	public GameState requestFullGameState() {
	    waitIfTooEarly();

	    Mono<ResponseEnvelope<GameState>> webAccess = baseWebClient
	            .method(HttpMethod.GET)
	            .uri("/" + gameID + "/states/" + playerID)
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {});

	    ResponseEnvelope<GameState> response = webAccess.block();
	    lastGameStateRequestTime = System.currentTimeMillis(); 

	    if (response.getState() == ERequestState.Error) {
	        throw new RuntimeException("Failed to retrieve full game state: " + response.getExceptionMessage());
	    }

	    return response.getData().orElseThrow(() ->
	            new RuntimeException("GameState data missing in server response."));
	}

	
	public ClientFullMap receiveFullMap() {
	    GameState gameState = requestFullGameState();
	    return MapConverter.convertToInternalMap(
	        gameState.getMap(),
	        new UniquePlayerIdentifier(playerID)
	    );
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

	    Field targetField = fields.get(next);
	    if (targetField == null || targetField.getTerrainType() == EGameTerrain.WATER) {
	        throw new RuntimeException("Attempted to move into a WATER or unknown field at " + next);
	    }

	    try {
	        Thread.sleep(50); // tiny delay to respect server processing time before sending move
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	    }
	    
	    Mono<ResponseEnvelope<Void>> responseMono = baseWebClient
	        .method(HttpMethod.POST)
	        .uri("/" + gameID + "/moves")
	        .body(BodyInserters.fromValue(PlayerMove.of(playerID, move)))
	        .retrieve()
	        .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<Void>>() {});

	    ResponseEnvelope<Void> response = responseMono.block();

	    if (response.getState() == ERequestState.Error) {
	        throw new RuntimeException("Move sending failed: " + response.getExceptionMessage());
	    }

	    System.out.println("Move " + move + " to " + next + " sent successfully.");
	}


	
	public EGameState waitUntilMustAct() throws InterruptedException {
	    while (true) {
	        Thread.sleep(400);  // min wait
	        EGameState state = requestGameState();
	        System.out.println("Current GameState: " + state);

	        if (state == EGameState.MUST_ACT) {
	            System.out.println("It's your turn!");
	            return state;
	        } else if (state == EGameState.WON || state == EGameState.LOST) {
	            System.out.println("Game over: " + state);
	            return state;
	        }
	    }
	}
	
	public Coordinate getCurrentServerPosition() {
	    GameState gameState = requestFullGameState();
	    return gameState.getMap().getMapNodes().stream()
	        .filter(node -> node.getPlayerPositionState().representsMyPlayer())
	        .findFirst()
	        .map(node -> new Coordinate(node.getX(), node.getY()))
	        .orElseThrow(() -> new IllegalStateException("Could not determine player's server position."));
	}
	
	private void waitIfTooEarly() {
	    long now = System.currentTimeMillis();
	    long waitTime = 500 - (now - lastGameStateRequestTime);
	    if (waitTime > 0) {
	        try {
	            Thread.sleep(waitTime);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }
	}

	
}
