package client.map;

import messagesbase.messagesfromserver.EPlayerPositionState;

public enum EPlayerPresence {
	MY_PLAYER,
    ENEMY_PLAYER,
    NO_PLAYER;
	
	public static EPlayerPresence fromServerPlayerPosition(EPlayerPositionState state) {
        if (state == EPlayerPositionState.MyPlayerPosition) {
            return MY_PLAYER;
        } else if (state == EPlayerPositionState.EnemyPlayerPosition) {
            return ENEMY_PLAYER;
        } else {
            return NO_PLAYER;
        }
    }
}
