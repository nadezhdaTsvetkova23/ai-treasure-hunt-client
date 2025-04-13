package client.networking;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import client.gamedata.EGameState;
import messagesbase.ResponseEnvelope;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ERequestState;
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

	    this.playerID = response.getData().get().getUniquePlayerID(); // Store it for reuse
	    return response.getData().get();
	}
	
	//public void sendHalfMap(HalfMap halfMap) {
	    // TODO: Convert your HalfMap into PlayerHalfMap using your converter
	    // TODO: POST it to /{gameId}/halfmaps/{playerId}
	//}

	//public void sendMove(EMove move) {
	    // TODO: Send a move (e.g. UP, DOWN) to /{gameId}/moves/{playerId}
	//}

	public EGameState requestGameState() {
	    if (playerID == null) {
	        throw new IllegalStateException("Player must be registered before requesting game state.");
	    }

	    Mono<ResponseEnvelope<GameState>> responseMono = baseWebClient
	            .method(HttpMethod.GET)
	            .uri("/" + gameID + "/states/" + playerID)
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {});

	    ResponseEnvelope<GameState> response = responseMono.block();

	    if (response.getState() == ERequestState.Error) {
	        throw new RuntimeException("Failed to get game state: " + response.getExceptionMessage());
	    }

	    GameState gameState = response.getData().orElseThrow(() ->
	        new RuntimeException("No GameState returned.")
	    );

	    // loop over all players and find your own state
	    for (PlayerState player : gameState.getPlayers()) {
	        if (player.getUniquePlayerID().equals(this.playerID)) {
	        	return EGameState.fromNetwork(player.getState());
	        }
	    }
	    throw new RuntimeException("Could not find own player in game state.");
	}


	//public FullMap receiveFullMap() {
	    // TODO: GET the full map and convert it to your own data structure
	//    return null;
	//}


}
