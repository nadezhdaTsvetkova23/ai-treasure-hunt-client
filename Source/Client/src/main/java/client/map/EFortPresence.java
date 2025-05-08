package client.map;

import messagesbase.messagesfromserver.EFortState;

public enum EFortPresence {
    MY_FORT,
    ENEMY_FORT,
    NO_FORT;

    public static EFortPresence fromServerFortState(EFortState fortState) {
        if (fortState == EFortState.MyFortPresent) {
            return MY_FORT;
        } else if (fortState == EFortState.EnemyFortPresent) {
            return ENEMY_FORT;
        } else {
            return NO_FORT;
        }
    }
}
