package client.exception;

//checked exception for player registration errors
public class PlayerRegistrationException extends Exception {
	
    public PlayerRegistrationException(String message) { 
    	super(message); 
    }
    
    public PlayerRegistrationException(String message, Throwable cause) { 
    	super(message, cause); 
    }
}
