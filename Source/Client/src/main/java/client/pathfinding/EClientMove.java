package client.pathfinding;

import messagesbase.messagesfromclient.EMove;

public enum EClientMove {
    UP,
    DOWN,
    LEFT,
    RIGHT;
    
	public EMove toServerEnum() {
        return switch (this) {
            case UP -> EMove.Up;
            case DOWN -> EMove.Down;
            case LEFT -> EMove.Left;
            case RIGHT -> EMove.Right;
        };
    }

}
