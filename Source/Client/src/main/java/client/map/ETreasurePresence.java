package client.map;

import messagesbase.messagesfromserver.ETreasureState;

public enum ETreasurePresence {
    TREASURE_PRESENT,
    NO_TREASURE;

    public static ETreasurePresence fromServerTreasureState(ETreasureState treasureState) {
        if (treasureState == ETreasureState.MyTreasureIsPresent) {
            return TREASURE_PRESENT;
        } else {
            return NO_TREASURE;
        }
    }
}
