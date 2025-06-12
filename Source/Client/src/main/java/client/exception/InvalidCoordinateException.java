// client.exception.InvalidCoordinateException.java
package client.exception;

//unchecked exception for invalid coordinates in the game
public class InvalidCoordinateException extends RuntimeException {
	
    public InvalidCoordinateException(String message) { 
    	super(message); 
    }
    
}
